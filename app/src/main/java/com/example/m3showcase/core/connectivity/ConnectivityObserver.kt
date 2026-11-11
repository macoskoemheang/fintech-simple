package com.example.m3showcase.core.connectivity

import kotlinx.coroutines.flow.Flow

enum class NetworkStatus { Available, Unavailable }

/**
 * COLD flow source: nothing happens until someone collects, and every collector
 * registers its own system callback. Use [NetworkMonitor] to share it.
 */
interface ConnectivityObserver {
    fun observe(): Flow<NetworkStatus>
    fun currentStatus(): NetworkStatus
}
