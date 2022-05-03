package com.scmp.mirror

import com.google.gson.Gson
import com.scmp.mirror.model.EventType
import com.scmp.mirror.model.MirrorErrorResponse
import com.scmp.mirror.util.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

/**
 * Created by wooyukit on 03,May,2022
 */
class MirrorCallback(private val eventType: EventType) : Callback<Unit> {
    private val gson = Gson()
    private val eventTypeValue = eventType

    override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
        when {
            response.code() == Constants.SUCCESS_RESPONSE -> {
                Timber.d("Mirror ${eventType.value} Request Response Success")
            }
            response.code() == Constants.ERROR_RESPONSE -> {
                /** parse the error message from server */
                try {
                    val errorDetailString = response.errorBody()?.string()
                    val errorDetail =
                        gson.fromJson(errorDetailString, MirrorErrorResponse::class.java)
                    Timber.e("Mirror ${eventType.value} Request Response Error: $errorDetail")
                } catch (e: Throwable) {
                    Timber.e("Mirror ${eventType.value} Request Response Unknown Error")
                }
            }
            else -> {
                Timber.e("Mirror Ping Request Response Unknown Error}")
            }
        }
    }

    override fun onFailure(call: Call<Unit>, t: Throwable) {
        Timber.e("Mirror Ping Request Error: ${t.localizedMessage}")
    }
}