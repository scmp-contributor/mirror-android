package com.scmp.mirror.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by wooyukit on 28,April,2022
 */
data class MirrorErrorResponse(
    @SerializedName("detail")
    @Expose
    var errorDetails: List<MirrorErrorDetail>? = null
)