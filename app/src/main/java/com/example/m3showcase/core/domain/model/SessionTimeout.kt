package com.example.m3showcase.core.domain.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * How long the app may stay idle (no touch, or sitting in the background) before the
 * session ends and the user is sent back to the PIN screen.
 */
enum class SessionTimeout(val duration: Duration) {
    /** Handy for trying the feature out; too short for real use. */
    FiveSeconds(5.seconds),
    FiveMinutes(5.minutes),
    TenMinutes(10.minutes),
    FifteenMinutes(15.minutes),
    ;

    companion object {
        val Default = FiveMinutes
    }
}
