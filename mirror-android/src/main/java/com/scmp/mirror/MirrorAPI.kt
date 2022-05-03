package com.scmp.mirror

import android.content.Context
import android.content.SharedPreferences
import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import com.scmp.mirror.model.EventType
import com.scmp.mirror.model.TrackData
import com.scmp.mirror.util.Constants.MIRROR_BASE_URL_PROD
import com.scmp.mirror.util.Constants.MIRROR_BASE_URL_UAT
import com.scmp.mirror.util.Constants.SCMP_ORGANIZATION_ID
import com.scmp.mirror.util.Constants.STORAGE_NAME
import com.scmp.mirror.util.Constants.STORAGE_USER_UUID
import com.scmp.mirror.util.MirrorService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber


/**
 * Created by wooyukit on 26,April,2022
 */
class MirrorAPI(
    context: Context,
    private val organizationId: String = SCMP_ORGANIZATION_ID,
    private val domain: String,
    isDebug: Boolean
) {

    private val mirrorService: MirrorService

    /** Sequence number of ping events within same session */
    private var sequenceNumber: Int = 1
    private var userUuid: String

    /** for storage the user uuid information */
    private var sharedPref: SharedPreferences

    companion object {
        /** shared instance for public use */
        lateinit var instance: MirrorAPI
    }

    init {
        /** init logger */
        if (isDebug) {
            Timber.plant(Timber.DebugTree())
        }

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

    fun ping(data: TrackData) {
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
            eventType = EventType.Ping.value
        )
        call.enqueue(MirrorCallback(EventType.Ping))
        sequenceNumber += 1
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
            eventType = EventType.Click.value
        )
        call.enqueue(MirrorCallback(EventType.Click))
        sequenceNumber += 1
    }
}