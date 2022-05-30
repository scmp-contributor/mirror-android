package com.scmp.mirror

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MotionEvent
import android.view.Window
import androidx.lifecycle.*
import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import com.scmp.mirror.model.*
import com.scmp.mirror.util.Constants.MAX_ENGAGEMENT_INTERVAL
import com.scmp.mirror.util.Constants.MAX_PING_INTERVAL
import com.scmp.mirror.util.Constants.MIRROR_BASE_URL_PROD
import com.scmp.mirror.util.Constants.MIRROR_BASE_URL_UAT
import com.scmp.mirror.util.Constants.PING_INTERVAL
import com.scmp.mirror.util.Constants.PING_INTERVAL_ACTIVE
import com.scmp.mirror.util.Constants.PING_INTERVAL_BACKGROUND
import com.scmp.mirror.util.Constants.SCMP_ORGANIZATION_ID
import com.scmp.mirror.util.Constants.STORAGE_NAME
import com.scmp.mirror.util.Constants.STORAGE_USER_UUID
import com.scmp.mirror.util.Constants.USER_AGENT_MOBILE
import com.scmp.mirror.util.Constants.USER_AGENT_TABLET
import com.scmp.mirror.util.MirrorService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.min
import kotlin.math.round

/**
 * Created by wooyukit on 26,April,2022
 */
class MirrorAPI(
    application: Application,
    private val organizationId: String = SCMP_ORGANIZATION_ID,
    private var domain: String,
    isDebug: Boolean,
    private val isTablet: Boolean = false
) : LifecycleObserver {

    private val mirrorService: MirrorService

    /** Sequence number of ping events within same session */
    private var sequenceNumber: Int = 1
    private var userUuid: String
    private var pageSectionId: String? = null

    /** for storage the user uuid information */
    private var sharedPref: SharedPreferences

    /** for idle mode to ping every interval */
    private var lastPingData: TrackData? = null
    private lateinit var timerToPing: CountDownTimer
    private var currentPingInterval = PING_INTERVAL_ACTIVE

    private lateinit var engageTimer: CountDownTimer
    private var lastTouchTime: Long? = null
    private var engageTime = 0

    /** Current Mode State */
    private var currentActiveMode: ActiveMode = ActiveMode.ACTIVE

    companion object {
        /** shared instance for public use */
        lateinit var instance: MirrorAPI
    }

    init {
        initLifecycleObserver(application)

        /** init retrofit for api call */
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl(if (isDebug) MIRROR_BASE_URL_UAT else MIRROR_BASE_URL_PROD)
            .addConverterFactory(GsonConverterFactory.create())

        val clientBuilder = OkHttpClient.Builder()
        val userAgentInterceptor = Interceptor { chain ->
            val request = chain.request()
            val requestWithUserAgent = request.newBuilder()
                .header("User-Agent", if (isTablet) USER_AGENT_TABLET else USER_AGENT_MOBILE)
                .build()
            chain.proceed(requestWithUserAgent)
        }
        clientBuilder.addInterceptor(userAgentInterceptor)

        /** add debug interceptor for api call when debugging */
        if (isDebug) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BASIC
            clientBuilder.addInterceptor(interceptor)
        }

        retrofitBuilder.client(clientBuilder.build())
        /** init api call service */
        mirrorService = retrofitBuilder.build().create(MirrorService::class.java)

        /** restore user uuid */
        sharedPref = application.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE)
        val storedUserUuid = sharedPref.getString(STORAGE_USER_UUID, null)
        if (storedUserUuid == null) {
            userUuid = NanoIdUtils.randomNanoId()
            with(sharedPref.edit()) {
                putString(STORAGE_USER_UUID, userUuid)
                apply()
            }
        } else {
            userUuid = storedUserUuid
        }

        initTimer()

        /** set shared instance for public use */
        instance = this

        /** log the initial information */
        Timber.d(
            "Mirror init with organization id: $organizationId \n" +
                    "domain: $domain \n" +
                    "isDebug: $isDebug \n" +
                    "user uuid: $userUuid"
        )
    }

    /** init lifecycle observer */
    private fun initLifecycleObserver(application: Application) {
        /** init life cycle observer */
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        /**
         *  Callbacks in ActivityLifecycle
         *  Add other lifecycle related callbacks(e.g. onStart, onPause and onStop) here if necessary
         */
        application.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityDestroyed(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                Timber.d("Mirror Activity Created")
                /** reset the ping information */
                timerToPing.cancel()
                engageTimer.cancel()
                engageTime = 0
                lastTouchTime = null
                sequenceNumber = 1
                lastPingData = null
            }

            override fun onActivityResumed(activity: Activity) {}
        })
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        Timber.d("Mirror App Foreground")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        Timber.d("Mirror App Background")
        currentActiveMode = ActiveMode.BACKGROUND
        timerToPing.cancel()
        timerToPing.start()
        lastTouchTime = null
        lastPingData?.let { ping(it, isForcePing = true) }
    }

    /** timer to re-ping server again */
    private fun initTimer() {
        timerToPing = object : CountDownTimer(MAX_PING_INTERVAL, PING_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                if (millisUntilFinished < MAX_PING_INTERVAL - PING_INTERVAL) {
                    val startedTime = ((MAX_PING_INTERVAL - millisUntilFinished) / 1000).toInt()
                    Timber.d("Mirror timer to ping started : $startedTime")
                    if (startedTime == currentPingInterval) {
                        lastPingData?.let { ping(it, isIntervalPing = true) }
                    }
                }
            }

            override fun onFinish() {
            }
        }
        engageTimer = object : CountDownTimer(MAX_ENGAGEMENT_INTERVAL, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (millisUntilFinished < MAX_ENGAGEMENT_INTERVAL) {
                    engageTime += 1
                    Timber.d("Mirror engage time increase : $engageTime")
                }
            }

            override fun onFinish() {
            }
        }
    }

    /** touch event */
    fun dispatchTouchEvent(ev: MotionEvent?) {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                Timber.d("Mirror Touch Action Down")
                /** fill in the touch time gap within 5 seconds */
                lastTouchTime?.let {
                    val timeDiff = ((Date().time - it) / 1000).toInt()
                    if (timeDiff <= 5) {
                        engageTime += timeDiff
                    }
                }
                engageTimer.cancel()
                engageTimer.start()

                if (currentActiveMode == ActiveMode.INACTIVE) {
                    currentActiveMode = ActiveMode.ACTIVE
                    currentPingInterval = PING_INTERVAL_ACTIVE
                    lastPingData?.let { ping(it, isForcePing = true) }
                    engageTime = 0
                    return
                }
            }
            MotionEvent.ACTION_UP -> {
                Timber.d("Mirror Touch Action Up")
                engageTimer.cancel()
                lastTouchTime = Date().time
            }
        }
    }

    /** public functions */
    fun ping(data: TrackData, isForcePing: Boolean = false, isIntervalPing: Boolean = false) {
        val ff: Int
        if (isForcePing) {
            ff = 0
            currentPingInterval =
                currentActiveMode.getNextPingIntervalAfterModeChanged(currentPingInterval)
        } else if (isIntervalPing) {
            /** check mode change */
            if (currentActiveMode == ActiveMode.ACTIVE && engageTime == 0) {
                currentActiveMode = ActiveMode.INACTIVE
                currentPingInterval =
                    currentActiveMode.getNextPingIntervalAfterModeChanged(currentPingInterval)
            } else {
                currentPingInterval = currentActiveMode.getNextPingInterval(currentPingInterval)
            }
            ff = min(2 * currentPingInterval, 270)
        } else {
            /** back from background mode and re-ping, defined as force ping */
            if (currentActiveMode == ActiveMode.BACKGROUND && lastPingData?.path == data.path) {
                ff = 0
            } else {
                sequenceNumber = 1
                pageSectionId = NanoIdUtils.randomNanoId()
                ff = 45
            }
            lastTouchTime = null
            engageTime = 0
            currentActiveMode = ActiveMode.ACTIVE
            currentPingInterval = PING_INTERVAL_ACTIVE
        }

        val call = mirrorService.ping(
            organizationId = organizationId,
            domain = domain,
            path = data.path,
            uuid = userUuid,
            visitorType = data.visitorType.type,
            sequenceNumber = sequenceNumber,
            section = data.section,
            author = data.author,
            pageTitle = data.pageTitle,
            pageSectionId = pageSectionId,
            internalReferrer = data.internalReferrer,
            externalReferrer = data.externalReferrer,
            eventType = EventType.Ping.value,
            engagedTime = min(engageTime, 15),
            ff = ff
        )
        call.enqueue(MirrorCallback(EventType.Ping))
        sequenceNumber += 1
        lastPingData = data
        engageTime = 0
        timerToPing.cancel()
        timerToPing.start()
        Timber.d("Mirror current mode ${currentActiveMode.name} + current ping interval $currentPingInterval")
    }

    fun click(data: TrackData) {
        val call = mirrorService.ping(
            organizationId = organizationId,
            domain = domain,
            path = data.path,
            uuid = userUuid,
            visitorType = data.visitorType.type,
            sequenceNumber = sequenceNumber,
            pageSectionId = pageSectionId,
            eventType = EventType.Click.value,
            engagedTime = engageTime,
            ci = data.clickUrl
        )
        call.enqueue(MirrorCallback(EventType.Click))
        sequenceNumber += 1
    }

    fun updateDomain(domain: String) {
        this.domain = domain
    }

    fun domainUrlAlias(urlAlias: String?): String {
        return "https://$domain${urlAlias.orEmpty()}"
    }
}