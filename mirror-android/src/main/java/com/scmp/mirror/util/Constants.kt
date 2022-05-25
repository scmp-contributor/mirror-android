package com.scmp.mirror.util

/**
 * Created by wooyukit on 21,April,2022
 */
object Constants {
    const val MIRROR_BASE_URL_PROD = "https://mirror.i-scmp.com/"
    const val MIRROR_BASE_URL_UAT = "https://uat-mirror.i-scmp.com/"
    const val AGENT_VERSION = "ma-0.2.0"
    const val SCMP_ORGANIZATION_ID = "1"
    const val ERROR_RESPONSE = 422
    const val SUCCESS_RESPONSE = 200
    const val STORAGE_NAME = "scmp-mirror-storage"
    const val STORAGE_USER_UUID = "user_uuid"
    const val PING_INTERVAL = 15000L
    const val MAX_PING_INTERVAL = 1000000000L
    const val MAX_ENGAGEMENT_INTERVAL = 1000000000L
    const val USER_AGENT_MOBILE = "native-mobile-android"
    const val USER_AGENT_TABLET = "native-tablet-android"
    const val PING_INTERVAL_ACTIVE = 15
    val PING_INTERVAL_INACTIVE = listOf(15, 30, 45, 75, 135, 255)
    val PING_INTERVAL_BACKGROUND = listOf(30, 45, 75, 165, 1005)
    const val NO_SECTION = "No Section"
    const val NO_AUTHOR = "No Author"
}