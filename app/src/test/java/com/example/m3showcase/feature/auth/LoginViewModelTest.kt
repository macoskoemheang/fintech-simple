package com.example.m3showcase.feature.auth

import com.example.m3showcase.core.domain.model.BankError
import com.example.m3showcase.core.domain.model.UserProfile
import com.example.m3showcase.core.domain.repository.AuthRepository
import com.example.m3showcase.feature.auth.domain.usecase.LoginWithPinUseCase
import com.example.m3showcase.feature.auth.presentation.LoginViewAction
import com.example.m3showcase.feature.auth.presentation.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private class FakeAuthRepository(private val validPin: String) : AuthRepository {
        override val session = MutableStateFlow<UserProfile?>(null)
        override suspend fun login(pin: String): Result<UserProfile> =
            if (pin == validPin) Result.success(UserProfile("1", "Test", "0")).onSuccess { session.value = it }
            else Result.failure(BankError.InvalidPin)
        override suspend fun logout() { session.value = null }
    }

    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @After fun tearDown() = Dispatchers.resetMain()

    private fun LoginViewModel.enter(pin: String) = pin.forEach { onAction(LoginViewAction.OnDigitClicked(it)) }

    @Test
    fun `correct PIN starts a session`() = runTest {
        val repo = FakeAuthRepository("1234")
        val vm = LoginViewModel(LoginWithPinUseCase(repo))
        vm.enter("1234")
        assertNotNull(repo.session.value)
        assertNull(vm.viewState.value.error)
    }

    @Test
    fun `wrong PIN clears input, shows error and triggers shake`() = runTest {
        val vm = LoginViewModel(LoginWithPinUseCase(FakeAuthRepository("1234")))
        vm.enter("0000")
        val state = vm.viewState.value
        assertEquals("", state.pin)
        assertNotNull(state.error)
        assertEquals(1, state.failureCount)
    }

    @Test
    fun `delete removes the last digit`() = runTest {
        val vm = LoginViewModel(LoginWithPinUseCase(FakeAuthRepository("9999")))
        vm.enter("12")
        vm.onAction(LoginViewAction.OnDeleteClicked)
        assertEquals("1", vm.viewState.value.pin)
    }
}
