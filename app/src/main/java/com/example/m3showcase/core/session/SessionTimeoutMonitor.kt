package com.example.m3showcase.core.session

import com.example.m3showcase.core.common.MonotonicClock
import com.example.m3showcase.core.domain.model.SessionTimeout
import com.example.m3showcase.core.domain.repository.AuthRepository
import com.example.m3showcase.core.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration

/**
 * Ends the session after [SessionTimeout] of inactivity. App-scoped (one per process),
 * like NetworkMonitor.
 *
 * Two paths cover every case:
 *  1. Foreground: a timer restarts on every interaction and logs out when it runs out.
 *  2. Back from background: [checkExpired] compares real elapsed time, because the
 *     timer may not fire on time while the device dozes.
 *
 * Logging out clears the session; BankApp already reacts to that by showing Login.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SessionTimeoutMonitor(
    private val authRepository: AuthRepository,
    settingsRepository: SettingsRepository,
    private val clock: MonotonicClock,
    private val appScope: CoroutineScope,
) {
    private val lastActivity = MutableStateFlow(clock.now())

    private val timeout: StateFlow<Duration> = settingsRepository.settings
        .map { it.sessionTimeout.duration }
        .distinctUntilChanged()
        .stateIn(appScope, SharingStarted.Eagerly, SessionTimeout.Default.duration)

    private val isLoggedIn = authRepository.session
        .map { it != null }
        .distinctUntilChanged()
        // A fresh login starts a fresh idle window.
        .onEach { loggedIn -> if (loggedIn) onUserActivity() }

    init {
        appScope.launch {
            combine(isLoggedIn, timeout, lastActivity) { loggedIn, timeout, last -> Triple(loggedIn, timeout, last) }
                // collectLatest cancels the running timer on every new interaction or setting change.
                .collectLatest { (loggedIn, timeout, last) ->
                    if (!loggedIn) return@collectLatest
                    delay(timeout - (clock.now() - last))
                    authRepository.logout()
                }
        }
    }

    /** Call on every user interaction (touch down / key press). Cheap: just stores a timestamp. */
    fun onUserActivity() {
        lastActivity.value = clock.now()
    }

    /** Call when the app comes back to the foreground. Logs out right away if the idle window has passed. */
    fun checkExpired() {
        val expired = clock.now() - lastActivity.value >= timeout.value
        if (expired && authRepository.session.value != null) {
            appScope.launch { authRepository.logout() }
        }
    }
}
