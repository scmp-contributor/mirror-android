package com.scmp.mirror.model

import com.scmp.mirror.util.Constants.PING_INTERVAL_ACTIVE
import com.scmp.mirror.util.Constants.PING_INTERVAL_BACKGROUND
import com.scmp.mirror.util.Constants.PING_INTERVAL_INACTIVE

/**
 * Created by wooyukit on 03,May,2022
 */
/**
 *
 */

enum class ActiveMode {
    ACTIVE, INACTIVE, BACKGROUND
}

fun ActiveMode.getNextPingInterval(currentInterval: Int) : Int {
    when(this) {
        ActiveMode.ACTIVE -> return PING_INTERVAL_ACTIVE
        ActiveMode.INACTIVE -> {
            PING_INTERVAL_INACTIVE.forEach {
                if (it > currentInterval) {
                    return it
                }
            }
            return PING_INTERVAL_INACTIVE.last()
        }
        ActiveMode.BACKGROUND -> {
            PING_INTERVAL_BACKGROUND.forEach {
                if (it > currentInterval) {
                    return it
                }
            }
            return PING_INTERVAL_BACKGROUND.last()
        }
    }
}

fun ActiveMode.getNextPingIntervalAfterModeChanged(currentInterval: Int) : Int {
    when(this) {
        ActiveMode.ACTIVE -> return PING_INTERVAL_ACTIVE
        ActiveMode.INACTIVE -> {
            PING_INTERVAL_INACTIVE.forEach {
                if (it >= currentInterval) {
                    return it
                }
            }
            return PING_INTERVAL_INACTIVE.last()
        }
        ActiveMode.BACKGROUND -> {
            PING_INTERVAL_BACKGROUND.forEach {
                if (it >= currentInterval) {
                    return it
                }
            }
            return PING_INTERVAL_BACKGROUND.last()
        }
    }
}