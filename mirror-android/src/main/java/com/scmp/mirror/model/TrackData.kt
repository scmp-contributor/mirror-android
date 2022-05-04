package com.scmp.mirror.model

/**
 * Created by wooyukit on 28,April,2022
 */
data class TrackData(
    /** The clean URL path without query strings. (Usually from canonical URL) */
    var path: String,
    var visitorType: VisitorType = VisitorType.Guest,
    /** The page section of the article */
    var section: String? = null,
    /** The page authors of the article */
    var author: String? = null,
    /** The page title */
    var pageTitle: String? = null,
    /** The page referrer from same domain */
    var internalReferrer: String? = null,
    /** The page referrer from other domain */
    var externalReferrer: String? = null,
)