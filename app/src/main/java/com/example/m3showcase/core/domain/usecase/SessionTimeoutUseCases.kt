package com.example.m3showcase.core.domain.usecase

import com.example.m3showcase.core.domain.model.SessionTimeout
import com.example.m3showcase.core.domain.repository.SettingsRepository
import com.example.m3showcase.core.session.SessionTimeoutMonitor

class SetSessionTimeoutUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(timeout: SessionTimeout) = repository.setSessionTimeout(timeout)
}

/** Resets the idle timer. */
class RecordUserActivityUseCase(private val monitor: SessionTimeoutMonitor) {
    operator fun invoke() = monitor.onUserActivity()
}

/** Ends the session if the app stayed idle in the background longer than the timeout. */
class CheckSessionExpiryUseCase(private val monitor: SessionTimeoutMonitor) {
    operator fun invoke() = monitor.checkExpired()
}
