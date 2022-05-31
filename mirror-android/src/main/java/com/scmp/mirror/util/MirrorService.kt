package com.scmp.mirror.util

import com.scmp.mirror.model.EventType
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by wooyukit on 28,April,2022
 */
interface MirrorService {
    @GET("ping")
    fun ping(
        /** The unique organization ID (uuid) from Mirror service. Each organization can hold multiple domains. Please get this value from Mirror team. */
        @Query("k") organizationId: String,
        /** The domain address that we implement the tracking. (Usually from canonical URL) */
        @Query("d") domain: String,
        /** The clean URL path without query strings. (Usually from canonical URL) */
        @Query("p") path: String,
        /** The unique ID for each visitor, generated on client side and store locally. 21 chars length by NanoID. */
        @Query("u") uuid: String,
        /** The visitor type.
        unc=Unclassified
        gst=Guest
        reg=Registered
        sub=Subscribed */
        @Query("vt") visitorType: String,
        /** The visitor engaged time on the page in seconds.*/
        @Query("eg") engagedTime: Int? = null,
        /** Sequence number of ping events within same session */
        @Query("sq") sequenceNumber: Int? = null,
        /** The page section of the article */
        @Query("s") section: String? = null,
        /** The page authors of the article */
        @Query("a") author: String? = null,
        /** The page title */
        @Query("pt") pageTitle: String? = null,
        /** The page session ID for correlating browsing behaviors under a single page. Generated on client side and store locally. 21 chars length by NanoID. */
        @Query("pi") pageSectionId: String? = null,
        /** The page referrer from same domain */
        @Query("ir") internalReferrer: String? = null,
        /** The page referrer from other domain */
        @Query("er") externalReferrer: String? = null,
        /** The interaction event type.
        ping: to indicate the current user is active
        click: to indicate a link was clicked */
        @Query("et") eventType: String = EventType.Ping.value,
        /** The flag to indicate if visitor accepts tracking */
        @Query("nc") nc: Boolean = false,
        /** The metadata of click event */
        @Query("ci") ci: String? = null,
        /** The additional duration added to the event to extend the page browsing session */
        @Query("ff") ff: Int? = null,
        /** Agent version */
        @Query("v") agentVersion: String = Constants.AGENT_VERSION
    ): Call<Unit>
}