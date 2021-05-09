package com.mittylabs.elaps.ui

import java.util.concurrent.TimeUnit

/**
 * milliseconds to readable timer format
 */
fun Long.toHumanFormat(): String {
    return String.format(
        "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(this),
        TimeUnit.MILLISECONDS.toMinutes(this) - TimeUnit.HOURS.toMinutes(
            TimeUnit.MILLISECONDS.toHours(this)
        ),
        TimeUnit.MILLISECONDS.toSeconds(this) - TimeUnit.MINUTES.toSeconds(
            TimeUnit.MILLISECONDS.toMinutes(this)
        )
    )
}