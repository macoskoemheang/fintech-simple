package com.example.m3showcase.feature.home.presentation

import androidx.compose.runtime.Immutable
import com.example.m3showcase.feature.home.domain.model.HomeDashboard
import com.example.m3showcase.core.ui.UiText
import com.example.m3showcase.R
import androidx.annotation.StringRes

@Immutable
sealed interface HomeViewState {
    data object Loading : HomeViewState
    data class Error(val message: UiText, val isOffline: Boolean) : HomeViewState
    data class Success(val dashboard: HomeDashboard, val isRefreshing: Boolean) : HomeViewState
}

/** Services on the home grid. Only some are built in this demo. */
enum class QuickAction(@StringRes val labelRes: Int) {
    Accounts(R.string.quick_accounts),
    Transfer(R.string.quick_transfers),
    Receive(R.string.quick_receive_qr),
    Payments(R.string.quick_payments),
    Cards(R.string.quick_cards),
    Deposits(R.string.quick_deposits),
    Loans(R.string.quick_loans),
    Locator(R.string.quick_locator),
}

sealed interface HomeViewAction {
    data object OnRefresh : HomeViewAction
    data object OnRetry : HomeViewAction
    data object OnToggleBalanceVisibility : HomeViewAction
    data class OnQuickActionClicked(val action: QuickAction) : HomeViewAction
    data class OnAccountClicked(val accountId: String) : HomeViewAction
    data class OnTransactionClicked(val transactionId: String) : HomeViewAction
    data object OnSeeAllTransactionsClicked : HomeViewAction
    data object OnNotificationsClicked : HomeViewAction
}

sealed interface HomeEvent {
    data class ShowMessage(val text: UiText) : HomeEvent
    data object OpenAccounts : HomeEvent
    data object OpenTransfer : HomeEvent
    data object OpenReceive : HomeEvent
    data object OpenNotifications : HomeEvent
    /** Payments, Deposits, Loans, Locator: each has its own screen. */
    data class OpenService(val action: QuickAction) : HomeEvent
    data class OpenTransactionHistory(val accountId: String?) : HomeEvent
    data class OpenReceipt(val transactionId: String) : HomeEvent
}
