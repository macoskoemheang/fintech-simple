package com.example.m3showcase.core.session

import com.example.m3showcase.core.common.MonotonicClock
import com.example.m3showcase.core.domain.model.EventTheme
import com.example.m3showcase.core.domain.model.SessionTimeout
import com.example.m3showcase.core.domain.model.ThemeMode
import com.example.m3showcase.core.domain.model.UserProfile
import com.example.m3showcase.core.domain.model.UserSettings
import com.example.m3showcase.core.domain.repository.AuthRepository
import com.example.m3showcase.core.domain.repository.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class SessionTimeoutMonitorTest {

    private class FakeAuthRepository : AuthRepository {
        override val session = MutableStateFlow<UserProfile?>(null)
        override suspend fun login(pin: String) = Result.success(UserProfile("1", "Test", "0")).onSuccess { session.value = it }
        override suspend fun logout() { session.value = null }
    }

    private class FakeSettingsRepository(timeout: SessionTimeout) : SettingsRepository {
        override val settings = MutableStateFlow(UserSettings(sessionTimeout = timeout))
        override suspend fun setThemeMode(mode: ThemeMode) = Unit
        override suspend fun setBalanceHidden(hidden: Boolean) = Unit
        override suspend fun setEventTheme(theme: EventTheme) = Unit
        override suspend fun setSessionTimeout(timeout: SessionTimeout) = settings.update { it.copy(sessionTimeout = timeout) }
    }

    /** Runs the monitor on the test's virtual clock. backgroundScope is canceled when the test ends. */
    private fun TestScope.setUp(timeout: SessionTimeout, clock: MonotonicClock = MonotonicClock { testScheduler.currentTime.milliseconds }) =
        Triple(FakeAuthRepository(), FakeSettingsRepository(timeout), clock).let { (auth, settings, c) ->
            val monitor = SessionTimeoutMonitor(auth, settings, c, backgroundScope)
            runCurrent()
            Triple(auth, settings, monitor)
        }

    private fun TestScope.advance(d: Duration) { advanceTimeBy(d); runCurrent() }

    @Test
    fun `logs out after the idle timeout`() = runTest {
        val (auth, _, _) = setUp(SessionTimeout.FiveSeconds)
        auth.login("1234"); runCurrent()

        advance(4.seconds); assertNotNull(auth.session.value)
        advance(1.seconds); assertNull(auth.session.value)
    }

    @Test
    fun `user activity restarts the countdown`() = runTest {
        val (auth, _, monitor) = setUp(SessionTimeout.FiveSeconds)
        auth.login("1234"); runCurrent()

        advance(4.seconds)
        monitor.onUserActivity(); runCurrent()
        advance(4.seconds); assertNotNull(auth.session.value)
        advance(1.seconds); assertNull(auth.session.value)
    }

    @Test
    fun `changing the setting applies the new timeout`() = runTest {
        val (auth, settings, _) = setUp(SessionTimeout.FiveSeconds)
        auth.login("1234"); runCurrent()

        settings.setSessionTimeout(SessionTimeout.TenMinutes); runCurrent()
        advance(9.minutes); assertNotNull(auth.session.value)
        advance(1.minutes); assertNull(auth.session.value)
    }

    @Test
    fun `returning from background after the timeout logs out immediately`() = runTest {
        // A clock that jumps ahead without the timer firing, like a dozing device.
        var now = 0.seconds
        val (auth, _, monitor) = setUp(SessionTimeout.FiveMinutes, clock = { now })
        auth.login("1234"); runCurrent()

        now += 6.minutes
        monitor.checkExpired(); runCurrent()
        assertNull(auth.session.value)
    }

    @Test
    fun `returning from background before the timeout keeps the session`() = runTest {
        var now = 0.seconds
        val (auth, _, monitor) = setUp(SessionTimeout.FiveMinutes, clock = { now })
        auth.login("1234"); runCurrent()

        now += 2.minutes
        monitor.checkExpired(); runCurrent()
        assertNotNull(auth.session.value)
    }
}
