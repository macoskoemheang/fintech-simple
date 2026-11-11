package com.example.m3showcase.feature.home.presentation

import androidx.compose.runtime.Immutable
import com.example.m3showcase.feature.home.domain.model.HomeDashboard

@Immutable
sealed interface HomeViewState {
    data object Loading : HomeViewState
    data class Error(val message: String, val isOffline: Boolean) : HomeViewState
    data class Success(val dashboard: HomeDashboard, val isRefreshing: Boolean) : HomeViewState
}

/** Services on the home grid. Only some are built in this demo. */
enum class QuickAction { Accounts, Transfer, Receive, Payments, Cards, Deposits, Loans, Locator }

sealed interface HomeViewAction {
    data object OnRefresh : HomeViewAction
    data object OnRetry : HomeViewAction
    data object OnToggleBalanceVisibility : HomeViewAction
    data class OnQuickActionClicked(val action: QuickAction) : HomeViewAction
    data class OnAccountClicked(val accountId: String) : HomeViewAction
    data class OnTransactionClicked(val transactionId: String) : HomeViewAction
    data object OnSeeAllTransactionsClicked : HomeViewAction
}

sealed interface HomeEvent {
    data class ShowMessage(val text: String) : HomeEvent
    data object OpenAccounts : HomeEvent
    data object OpenTransfer : HomeEvent
    data object OpenReceive : HomeEvent
    data class OpenTransactionHistory(val accountId: String?) : HomeEvent
    data class OpenReceipt(val transactionId: String) : HomeEvent
}
