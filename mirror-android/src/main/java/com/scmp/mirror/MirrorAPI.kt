package com.scmp.mirror

import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import com.google.gson.Gson
import com.scmp.mirror.model.EventType
import com.scmp.mirror.model.MirrorErrorResponse
import com.scmp.mirror.model.TrackData
import com.scmp.mirror.util.Constants.ERROR_RESPONSE
import com.scmp.mirror.util.Constants.MIRROR_BASE_URL_PROD
import com.scmp.mirror.util.Constants.MIRROR_BASE_URL_UAT
import com.scmp.mirror.util.Constants.SCMP_ORGANIZATION_ID
import com.scmp.mirror.util.Constants.SUCCESS_RESPONSE
import com.scmp.mirror.util.MirrorService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber


/**
 * Created by wooyukit on 26,April,2022
 */
class MirrorAPI(
    private val organizationId: String = SCMP_ORGANIZATION_ID,
    private val domain: String,
    isDebug: Boolean
) {

    private val mirrorService: MirrorService

    /** Sequence number of ping events within same session */
    private var sequenceNumber: Int = 1
    private var userUuid: String

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
        userUuid = NanoIdUtils.randomNanoId()
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
            externalReferrer = data.externalReferrer
        )
        call.enqueue(MirrorCallback(EventType.Ping))
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
            externalReferrer = data.externalReferrer
        )
        call.enqueue(MirrorCallback(EventType.Click))
    }
}