package com.example.m3showcase.feature.accounts.presentation.history

import androidx.compose.runtime.Immutable
import com.example.m3showcase.feature.accounts.domain.model.TransactionFilter
import com.example.m3showcase.feature.accounts.domain.model.TransactionHistory
import com.example.m3showcase.core.ui.UiText

@Immutable
sealed interface TransactionHistoryViewState {
    val filter: TransactionFilter

    data class Loading(override val filter: TransactionFilter) : TransactionHistoryViewState
    data class Error(override val filter: TransactionFilter, val message: UiText, val isOffline: Boolean) : TransactionHistoryViewState
    data class Success(
        override val filter: TransactionFilter,
        val history: TransactionHistory,
        val isRefreshing: Boolean,
    ) : TransactionHistoryViewState
}

sealed interface TransactionHistoryViewAction {
    data class OnFilterSelected(val filter: TransactionFilter) : TransactionHistoryViewAction
    data class OnTransactionClicked(val transactionId: String) : TransactionHistoryViewAction
    data object OnRefresh : TransactionHistoryViewAction
    data object OnBackClicked : TransactionHistoryViewAction
}

sealed interface TransactionHistoryEvent {
    data class ShowMessage(val text: UiText) : TransactionHistoryEvent
    data class OpenReceipt(val transactionId: String) : TransactionHistoryEvent
    data object NavigateBack : TransactionHistoryEvent
}
