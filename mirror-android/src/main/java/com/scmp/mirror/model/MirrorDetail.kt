package com.scmp.mirror.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by wooyukit on 28,April,2022
 */
data class MirrorDetail(
    @SerializedName("loc")
    @Expose
    var loc: List<String>? = null,

    @SerializedName("msg")
    @Expose
    var msg: String? = null,

    @SerializedName("type")
    @Expose
    var type: String? = null
)