package com.scmp.mirror.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by wooyukit on 28,April,2022
 */
data class MirrorResponse(
    @SerializedName("detail")
    @Expose
    var loc: List<MirrorDetail>? = null
)