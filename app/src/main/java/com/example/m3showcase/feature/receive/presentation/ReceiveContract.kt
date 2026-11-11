package com.example.m3showcase.feature.receive.presentation

import androidx.compose.runtime.Immutable
import com.example.m3showcase.core.domain.model.ReceiveQr

@Immutable
sealed interface ReceiveViewState {
    data object Loading : ReceiveViewState
    data object Empty : ReceiveViewState
    data class Error(val message: String, val isOffline: Boolean) : ReceiveViewState
    data class Success(val codes: List<ReceiveQr>, val selectedIndex: Int) : ReceiveViewState {
        val selected: ReceiveQr get() = codes[selectedIndex]
    }
}

sealed interface ReceiveViewAction {
    data class OnAccountSelected(val index: Int) : ReceiveViewAction
    data object OnShareClicked : ReceiveViewAction
    data object OnRetry : ReceiveViewAction
}

sealed interface ReceiveEvent {
    data class Share(val text: String) : ReceiveEvent
}
