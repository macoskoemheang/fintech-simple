package com.example.m3showcase.feature.accounts.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m3showcase.core.domain.model.BankError
import com.example.m3showcase.core.domain.usecase.RefreshBankDataUseCase
import com.example.m3showcase.core.ui.toUserMessage
import com.example.m3showcase.feature.accounts.domain.model.TransactionFilter
import com.example.m3showcase.feature.accounts.domain.model.TransactionHistory
import com.example.m3showcase.feature.accounts.domain.usecase.ObserveTransactionHistoryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** [accountId] comes from the Navigation 3 key (Destination.TransactionHistory). */
@OptIn(ExperimentalCoroutinesApi::class)
class TransactionHistoryViewModel(
    private val accountId: String?,
    private val observeHistory: ObserveTransactionHistoryUseCase,
    private val refreshBankData: RefreshBankDataUseCase,
) : ViewModel() {

    private val filter = MutableStateFlow(TransactionFilter.All)
    private val isRefreshing = MutableStateFlow(false)
    private val lastError = MutableStateFlow<Throwable?>(null)

    private val _events = Channel<TransactionHistoryEvent>(Channel.BUFFERED)
    val events: Flow<TransactionHistoryEvent> = _events.receiveAsFlow()

    /**
     * flatMapLatest: when the filter changes, the previous upstream subscription is
     * cancelled and a new one starts, so an old filter's results never show.
     */
    private val history: Flow<TransactionHistory?> = filter
        .flatMapLatest { f -> observeHistory(accountId, f) }
        .map<TransactionHistory, TransactionHistory?> { it }
        .onStart { emit(null) }

    val viewState: StateFlow<TransactionHistoryViewState> = combine(
        history, filter, isRefreshing, lastError,
    ) { history, filter, refreshing, error ->
        when {
            history != null -> TransactionHistoryViewState.Success(filter, history, refreshing)
            error != null -> TransactionHistoryViewState.Error(filter, error.toUserMessage(), error is BankError.NoInternet)
            else -> TransactionHistoryViewState.Loading(filter)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransactionHistoryViewState.Loading(TransactionFilter.All))

    init {
        refresh()
    }

    fun onAction(action: TransactionHistoryViewAction) {
        when (action) {
            is TransactionHistoryViewAction.OnFilterSelected -> filter.value = action.filter
            is TransactionHistoryViewAction.OnTransactionClicked ->
                _events.trySend(TransactionHistoryEvent.OpenReceipt(action.transactionId))
            TransactionHistoryViewAction.OnRefresh -> refresh()
            TransactionHistoryViewAction.OnBackClicked -> _events.trySend(TransactionHistoryEvent.NavigateBack)
        }
    }

    private fun refresh() {
        if (isRefreshing.value) return
        viewModelScope.launch {
            isRefreshing.value = true
            lastError.value = null
            refreshBankData().onFailure { error ->
                lastError.value = error
                if (viewState.value is TransactionHistoryViewState.Success) {
                    _events.send(TransactionHistoryEvent.ShowMessage(error.toUserMessage()))
                }
            }
            isRefreshing.value = false
        }
    }
}
