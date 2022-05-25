package com.scmp.mirror.model

import com.scmp.mirror.util.Constants.NO_AUTHOR
import com.scmp.mirror.util.Constants.NO_SECTION

/**
 * Created by wooyukit on 28,April,2022
 */
data class TrackData(
    /** The clean URL path without query strings. (Usually from canonical URL) */
    var path: String,
    var visitorType: VisitorType = VisitorType.Guest,
    /** The page section of the article */
    var section: String? = NO_SECTION,
    /** The page authors of the article */
    var author: String? = NO_AUTHOR,
    /** The page title */
    var pageTitle: String? = null,
    /** The page referrer from same domain */
    var internalReferrer: String? = null,
    /** The page referrer from other domain */
    var externalReferrer: String? = null,
    /** The metadata of click event */
    var clickUrl: String? = null
)