package com.example.m3showcase.core.connectivity

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Converts the COLD [ConnectivityObserver] flow into ONE HOT [StateFlow] for the whole app.
 *
 * - Only one NetworkCallback is registered, however many screens are listening.
 * - `.value` is always readable synchronously (the data layer checks it before calls).
 * - Eagerly: sharing starts right away and runs for the app's lifetime, because the data
 *   layer reads `isOnline` even when no screen is collecting.
 */
class NetworkMonitor(
    observer: ConnectivityObserver,
    appScope: CoroutineScope,
) {
    val status: StateFlow<NetworkStatus> = observer.observe()
        .stateIn(
            scope = appScope,
            started = SharingStarted.Eagerly,
            initialValue = observer.currentStatus(),
        )

    val isOnline: Boolean get() = status.value == NetworkStatus.Available
}
