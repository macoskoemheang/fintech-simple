package com.example.m3showcase.feature.auth.presentation

import androidx.compose.runtime.Immutable
import com.example.m3showcase.core.ui.UiText

/** A form screen, so ViewState is one data class rather than a sealed hierarchy. */
@Immutable
data class LoginViewState(
    val pin: String = "",
    val isLoading: Boolean = false,
    val error: UiText? = null,
    /** Goes up on each failure; the screen keys its shake animation on it. */
    val failureCount: Int = 0,
)

sealed interface LoginViewAction {
    data class OnDigitClicked(val digit: Char) : LoginViewAction
    data object OnDeleteClicked : LoginViewAction
    data object OnForgotPinClicked : LoginViewAction
}

sealed interface LoginEvent {
    data class ShowMessage(val text: UiText) : LoginEvent
}
