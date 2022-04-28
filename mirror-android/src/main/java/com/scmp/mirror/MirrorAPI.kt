package com.scmp.mirror

import android.content.Context
import com.scmp.mirror.util.MRConstants
import com.scmp.mirror.util.MirrorService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


/**
 * Created by wooyukit on 26,April,2022
 */
class MirrorAPI(context: Context, domain: String, isDebug: Boolean) {

    private val mirrorService: MirrorService

    companion object {
        lateinit var instance: MirrorAPI
    }

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(if (isDebug) MRConstants.MIRROR_BASE_URL_UAT else MRConstants.MIRROR_BASE_URL_PROD)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        mirrorService = retrofit.create(MirrorService::class.java)
        instance = this
    }

    fun ping() {
    }
}