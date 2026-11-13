package com.example.m3showcase.feature.receipt.presentation

import androidx.compose.runtime.Immutable
import com.example.m3showcase.core.domain.model.Transaction
import com.example.m3showcase.core.ui.UiText

@Immutable
sealed interface ReceiptViewState {
    data object Loading : ReceiptViewState
    data class Error(val message: UiText) : ReceiptViewState
    data class Success(val transaction: Transaction, val isNewTransfer: Boolean) : ReceiptViewState
}

sealed interface ReceiptViewAction {
    data object OnDoneClicked : ReceiptViewAction
    data object OnShareClicked : ReceiptViewAction
    data object OnRetry : ReceiptViewAction
}

sealed interface ReceiptEvent {
    data object Close : ReceiptEvent
    /** The UI words it in the user's language. */
    data class Share(val transaction: Transaction) : ReceiptEvent
}
