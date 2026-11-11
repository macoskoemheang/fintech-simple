package com.example.m3showcase.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m3showcase.core.connectivity.NetworkStatus
import com.example.m3showcase.core.domain.model.BankError
import com.example.m3showcase.core.domain.usecase.ObserveNetworkStatusUseCase
import com.example.m3showcase.core.domain.usecase.RefreshBankDataUseCase
import com.example.m3showcase.core.domain.usecase.SetBalanceHiddenUseCase
import com.example.m3showcase.core.ui.toUserMessage
import com.example.m3showcase.feature.home.domain.model.HomeDashboard
import com.example.m3showcase.feature.home.domain.usecase.ObserveHomeDashboardUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    observeDashboard: ObserveHomeDashboardUseCase,
    observeNetworkStatus: ObserveNetworkStatusUseCase,
    private val refreshBankData: RefreshBankDataUseCase,
    private val setBalanceHidden: SetBalanceHiddenUseCase,
) : ViewModel() {

    private data class RefreshStatus(val isRefreshing: Boolean = false, val error: Throwable? = null)

    private val refreshStatus = MutableStateFlow(RefreshStatus())

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events: Flow<HomeEvent> = _events.receiveAsFlow()

    /**
     * COLD dashboard flow + HOT refresh status → ONE HOT StateFlow for the UI.
     * WhileSubscribed(5_000) stops upstream work 5s after the screen goes away and
     * survives a rotation without restarting.
     */
    val viewState: StateFlow<HomeViewState> = combine(
        observeDashboard().map<HomeDashboard, HomeDashboard?> { it }.onStart { emit(null) },
        refreshStatus,
    ) { dashboard, status ->
        when {
            dashboard != null -> HomeViewState.Success(dashboard, isRefreshing = status.isRefreshing)
            status.error != null -> HomeViewState.Error(
                message = status.error.toUserMessage(),
                isOffline = status.error is BankError.NoInternet,
            )
            else -> HomeViewState.Loading
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeViewState.Loading)

    private var refreshJob: Job? = null

    init {
        refresh(showIndicator = false)

        // Connectivity: when the device comes back online and the last refresh
        // failed, retry on our own. drop(1) skips the StateFlow's current value.
        observeNetworkStatus()
            .drop(1)
            .filter { it == NetworkStatus.Available && refreshStatus.value.error != null }
            .onEach { refresh(showIndicator = false) }
            .launchIn(viewModelScope)
    }

    fun onAction(action: HomeViewAction) {
        when (action) {
            HomeViewAction.OnRefresh -> refresh(showIndicator = true)
            HomeViewAction.OnRetry -> {
                refreshStatus.value = RefreshStatus()
                refresh(showIndicator = false)
            }
            HomeViewAction.OnToggleBalanceVisibility -> toggleBalance()
            is HomeViewAction.OnQuickActionClicked -> onQuickAction(action.action)
            is HomeViewAction.OnAccountClicked -> send(HomeEvent.OpenTransactionHistory(action.accountId))
            is HomeViewAction.OnTransactionClicked -> send(HomeEvent.OpenReceipt(action.transactionId))
            HomeViewAction.OnSeeAllTransactionsClicked -> send(HomeEvent.OpenTransactionHistory(null))
        }
    }

    private fun onQuickAction(action: QuickAction) = send(
        when (action) {
            QuickAction.Accounts -> HomeEvent.OpenAccounts
            QuickAction.Transfer -> HomeEvent.OpenTransfer
            QuickAction.Receive -> HomeEvent.OpenReceive
            else -> HomeEvent.ShowMessage("${action.name} is coming soon")
        }
    )

    private fun refresh(showIndicator: Boolean) {
        if (refreshJob?.isActive == true) return // Ignore double pull-to-refresh.
        refreshJob = viewModelScope.launch {
            refreshStatus.update { it.copy(isRefreshing = showIndicator) }
            val result = refreshBankData()
            refreshStatus.value = RefreshStatus(isRefreshing = false, error = result.exceptionOrNull())

            // Data is already on screen: report the failure as a snackbar, not a full error page.
            val error = result.exceptionOrNull()
            if (error != null && viewState.value is HomeViewState.Success) {
                send(HomeEvent.ShowMessage(error.toUserMessage()))
            }
        }
    }

    private fun toggleBalance() {
        val current = (viewState.value as? HomeViewState.Success)?.dashboard?.isBalanceHidden ?: return
        viewModelScope.launch { setBalanceHidden(!current) }
    }

    private fun send(event: HomeEvent) {
        _events.trySend(event)
    }
}
