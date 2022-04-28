package com.scmp.mirror.model

import retrofit2.http.Query

/**
 * Created by wooyukit on 28,April,2022
 */
data class TrackData(
    /** The visitor type.
    unc=Unclassified
    gst=Guest
    reg=Registered
    sub=Subscribed */
    var visitorType: String? = null
)