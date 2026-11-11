package com.example.m3showcase.feature.accounts.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m3showcase.core.domain.model.Account
import com.example.m3showcase.core.domain.model.BankError
import com.example.m3showcase.core.domain.usecase.ObserveAccountsUseCase
import com.example.m3showcase.core.domain.usecase.ObserveSettingsUseCase
import com.example.m3showcase.core.domain.usecase.RefreshBankDataUseCase
import com.example.m3showcase.core.ui.toUserMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccountsViewModel(
    observeAccounts: ObserveAccountsUseCase,
    observeSettings: ObserveSettingsUseCase,
    private val refreshBankData: RefreshBankDataUseCase,
) : ViewModel() {

    private val isRefreshing = MutableStateFlow(false)
    private val lastError = MutableStateFlow<Throwable?>(null)

    private val _events = Channel<AccountsEvent>(Channel.BUFFERED)
    val events: Flow<AccountsEvent> = _events.receiveAsFlow()

    val viewState: StateFlow<AccountsViewState> = combine(
        observeAccounts().map<List<Account>, List<Account>?> { it }.onStart { emit(null) },
        observeSettings(),
        isRefreshing,
        lastError,
    ) { accounts, settings, refreshing, error ->
        when {
            accounts == null && error != null ->
                AccountsViewState.Error(error.toUserMessage(), isOffline = error is BankError.NoInternet)
            accounts == null -> AccountsViewState.Loading
            accounts.isEmpty() -> AccountsViewState.Empty
            else -> AccountsViewState.Success(accounts, settings.isBalanceHidden, refreshing)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountsViewState.Loading)

    init {
        refresh()
    }

    fun onAction(action: AccountsViewAction) {
        when (action) {
            AccountsViewAction.OnRefresh -> refresh()
            is AccountsViewAction.OnAccountClicked -> _events.trySend(AccountsEvent.OpenHistory(action.accountId))
            is AccountsViewAction.OnTransferClicked -> _events.trySend(AccountsEvent.OpenTransfer(action.accountId))
        }
    }

    private fun refresh() {
        if (isRefreshing.value) return
        viewModelScope.launch {
            isRefreshing.value = true
            lastError.value = null
            refreshBankData().onFailure { error ->
                lastError.value = error
                if (viewState.value is AccountsViewState.Success) {
                    _events.send(AccountsEvent.ShowMessage(error.toUserMessage()))
                }
            }
            isRefreshing.value = false
        }
    }
}
