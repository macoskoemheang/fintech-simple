package com.example.m3showcase.feature.accounts.presentation.list

import androidx.compose.runtime.Immutable
import com.example.m3showcase.core.domain.model.Account

@Immutable
sealed interface AccountsViewState {
    data object Loading : AccountsViewState
    data object Empty : AccountsViewState
    data class Error(val message: String, val isOffline: Boolean) : AccountsViewState
    data class Success(
        val accounts: List<Account>,
        val isBalanceHidden: Boolean,
        val isRefreshing: Boolean,
    ) : AccountsViewState
}

sealed interface AccountsViewAction {
    data object OnRefresh : AccountsViewAction
    data class OnAccountClicked(val accountId: String) : AccountsViewAction
    data class OnTransferClicked(val accountId: String) : AccountsViewAction
}

sealed interface AccountsEvent {
    data class ShowMessage(val text: String) : AccountsEvent
    data class OpenHistory(val accountId: String) : AccountsEvent
    data class OpenTransfer(val accountId: String) : AccountsEvent
}
