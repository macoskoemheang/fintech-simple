package com.example.m3showcase.core.data.remote

import com.example.m3showcase.core.common.DispatcherProvider
import com.example.m3showcase.core.connectivity.NetworkMonitor
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Shared "HTTP round-trip" for the fake data sources: offline check, latency, then the
 * server-side work under a lock. Throws IOException when offline, like Retrofit would,
 * so [safeApiCall] maps it to BankError.NoInternet.
 */
class FakeNetwork(
    private val networkMonitor: NetworkMonitor,
    private val dispatchers: DispatcherProvider,
) {
    private val mutex = Mutex()

    suspend fun <T> call(latencyMs: Long = 700, block: () -> T): T = withContext(dispatchers.io) {
        if (!networkMonitor.isOnline) throw IOException("Unable to resolve host")
        delay(latencyMs)
        if (!networkMonitor.isOnline) throw IOException("Connection reset")
        mutex.withLock { block() }
    }
}
