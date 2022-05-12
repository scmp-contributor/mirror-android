package com.scmp.mirror

import android.content.Context
import android.content.SharedPreferences
import android.os.CountDownTimer
import androidx.lifecycle.*
import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import com.scmp.mirror.model.EventType
import com.scmp.mirror.model.TrackData
import com.scmp.mirror.util.Constants.MAX_ENGAGEMENT_INTERVAL
import com.scmp.mirror.util.Constants.MAX_PING_INTERVAL
import com.scmp.mirror.util.Constants.MIRROR_BASE_URL_PROD
import com.scmp.mirror.util.Constants.MIRROR_BASE_URL_UAT
import com.scmp.mirror.util.Constants.PING_INTERVAL
import com.scmp.mirror.util.Constants.SCMP_ORGANIZATION_ID
import com.scmp.mirror.util.Constants.STORAGE_NAME
import com.scmp.mirror.util.Constants.STORAGE_USER_UUID
import com.scmp.mirror.util.MirrorService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.*

/**
 * Created by wooyukit on 26,April,2022
 */
class MirrorAPI(
    context: Context,
    private val organizationId: String = SCMP_ORGANIZATION_ID,
    private val domain: String,
    isDebug: Boolean
) : LifecycleObserver {

    private val mirrorService: MirrorService

    /** Sequence number of ping events within same session */
    private var sequenceNumber: Int = 1
    private var userUuid: String

    /** for storage the user uuid information */
    private var sharedPref: SharedPreferences

    /** for idle mode to ping every interval */
    private var lastPingData: TrackData? = null
    private lateinit var timerToPing: CountDownTimer

    private lateinit var engageTimer: CountDownTimer
    private var engageTime = 0


    companion object {
        /** shared instance for public use */
        lateinit var instance: MirrorAPI
    }

    init {
        /** init life cycle observer */
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        /** init retrofit for api call */
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl(if (isDebug) MIRROR_BASE_URL_UAT else MIRROR_BASE_URL_PROD)
            .addConverterFactory(GsonConverterFactory.create())

        /** add debug interceptor for api call when debugging */
        if (isDebug) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BASIC
            val client = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build()
            retrofitBuilder.client(client)
        }
        /** init api call service */
        mirrorService = retrofitBuilder.build().create(MirrorService::class.java)

        /** restore user uuid */
        sharedPref = context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE)
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

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        Timber.d("Mirror App Foreground")
        lastPingData?.let { ping(it) }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        Timber.d("Mirror App Background")
        timerToPing.cancel()
        engageTimer.cancel()
    }

    /** timer to re-ping server again */
    private fun initTimer() {
        timerToPing = object : CountDownTimer(MAX_PING_INTERVAL, PING_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                if (millisUntilFinished < MAX_PING_INTERVAL - PING_INTERVAL) {
                    lastPingData?.let { ping(it, isForcePing = true) }
                }
            }

            override fun onFinish() {
            }
        }
        engageTimer = object : CountDownTimer(MAX_ENGAGEMENT_INTERVAL, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (millisUntilFinished < MAX_ENGAGEMENT_INTERVAL - 1000) {
                    engageTime += 1
                }
            }

            override fun onFinish() {
            }
        }
    }

    /** public functions */
    fun ping(data: TrackData, isForcePing: Boolean = false) {
        /** restart the timer */
        if (!isForcePing) {
            timerToPing.cancel()
            timerToPing.start()
            engageTimer.cancel()
            engageTimer.start()
            engageTime = 0
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
            internalReferrer = data.internalReferrer,
            externalReferrer = data.externalReferrer,
            eventType = EventType.Ping.value,
            engagedTime = engageTime
        )
        call.enqueue(MirrorCallback(EventType.Ping))
        sequenceNumber += 1
        lastPingData = data
    }

    fun click(data: TrackData) {
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
            internalReferrer = data.internalReferrer,
            externalReferrer = data.externalReferrer,
            eventType = EventType.Click.value,
            engagedTime = engageTime
        )
        call.enqueue(MirrorCallback(EventType.Click))
        sequenceNumber += 1
    }
}