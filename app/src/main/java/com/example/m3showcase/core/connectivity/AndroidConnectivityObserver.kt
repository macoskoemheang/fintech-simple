package com.example.m3showcase.core.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.core.content.getSystemService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged

class AndroidConnectivityObserver(context: Context) : ConnectivityObserver {

    private val connectivityManager = requireNotNull(context.getSystemService<ConnectivityManager>())

    /**
     * callbackFlow turns the listener-based ConnectivityManager API into a cold Flow.
     * The callback is registered when collection starts and removed in awaitClose,
     * so the flow can never leak a callback.
     */
    override fun observe(): Flow<NetworkStatus> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                trySend(caps.toStatus())
            }

            override fun onLost(network: Network) {
                trySend(NetworkStatus.Unavailable)
            }

            override fun onUnavailable() {
                trySend(NetworkStatus.Unavailable)
            }
        }

        trySend(currentStatus()) // Emit the state right away, not only on change.
        connectivityManager.registerDefaultNetworkCallback(callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }
        .distinctUntilChanged()
        .conflate()

    override fun currentStatus(): NetworkStatus {
        val caps = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return caps?.toStatus() ?: NetworkStatus.Unavailable
    }

    private fun NetworkCapabilities.toStatus(): NetworkStatus {
        // VALIDATED = the system confirmed this network can reach the internet.
        val online = hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        return if (online) NetworkStatus.Available else NetworkStatus.Unavailable
    }
}
