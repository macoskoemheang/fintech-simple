package com.example.m3showcase.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m3showcase.core.ui.toUserMessage
import com.example.m3showcase.feature.auth.domain.usecase.LoginWithPinUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Navigation after login is driven by the session (see MainViewModel), not by an
 * event. That way a restored back stack can never show bank data without a session.
 */
class LoginViewModel(
    private val loginWithPin: LoginWithPinUseCase,
) : ViewModel() {

    private val _viewState = MutableStateFlow(LoginViewState())
    val viewState: StateFlow<LoginViewState> = _viewState.asStateFlow()

    // Channel-backed Flow: each event reaches exactly one collector, and events are
    // buffered (not lost) while the screen is in the background.
    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events: Flow<LoginEvent> = _events.receiveAsFlow()

    fun onAction(action: LoginViewAction) {
        when (action) {
            is LoginViewAction.OnDigitClicked -> appendDigit(action.digit)
            LoginViewAction.OnDeleteClicked -> _viewState.update { it.copy(pin = it.pin.dropLast(1), error = null) }
            LoginViewAction.OnForgotPinClicked ->
                _events.trySend(LoginEvent.ShowMessage("Visit any branch or call 023 000 000 to reset your PIN."))
        }
    }

    private fun appendDigit(digit: Char) {
        val state = _viewState.value
        if (state.isLoading || state.pin.length >= LoginWithPinUseCase.PIN_LENGTH) return
        val pin = state.pin + digit
        _viewState.update { it.copy(pin = pin, error = null) }
        if (pin.length == LoginWithPinUseCase.PIN_LENGTH) submit(pin)
    }

    private fun submit(pin: String) {
        viewModelScope.launch {
            _viewState.update { it.copy(isLoading = true) }
            loginWithPin(pin)
                .onSuccess { _viewState.update { it.copy(isLoading = false) } }
                .onFailure { error ->
                    _viewState.update {
                        it.copy(
                            pin = "",
                            isLoading = false,
                            error = error.toUserMessage(),
                            failureCount = it.failureCount + 1,
                        )
                    }
                }
        }
    }
}
