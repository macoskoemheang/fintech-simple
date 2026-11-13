package com.example.m3showcase.core.common

import android.os.SystemClock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/** Injected so tests can control time. Must never jump backwards when the user changes the wall clock. */
fun interface MonotonicClock {
    fun now(): Duration
}

/**
 * elapsedRealtime keeps counting while the device is asleep, unlike System.nanoTime
 * (which coroutine delay() uses). That matters for the background check on resume.
 */
class SystemMonotonicClock : MonotonicClock {
    override fun now(): Duration = SystemClock.elapsedRealtime().milliseconds
}
