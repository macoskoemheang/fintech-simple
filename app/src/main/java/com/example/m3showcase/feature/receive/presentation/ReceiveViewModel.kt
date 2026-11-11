package com.example.m3showcase.feature.receive.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m3showcase.core.domain.model.BankError
import com.example.m3showcase.core.domain.model.ReceiveQr
import com.example.m3showcase.core.domain.usecase.RefreshBankDataUseCase
import com.example.m3showcase.core.ui.formatAccountNumber
import com.example.m3showcase.core.ui.toUserMessage
import com.example.m3showcase.feature.receive.domain.usecase.ObserveReceiveQrUseCase
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

class ReceiveViewModel(
    observeReceiveQr: ObserveReceiveQrUseCase,
    private val refreshBankData: RefreshBankDataUseCase,
) : ViewModel() {

    private val selectedIndex = MutableStateFlow(0)
    private val loadError = MutableStateFlow<Throwable?>(null)

    private val _events = Channel<ReceiveEvent>(Channel.BUFFERED)
    val events: Flow<ReceiveEvent> = _events.receiveAsFlow()

    val viewState: StateFlow<ReceiveViewState> = combine(
        observeReceiveQr().map<List<ReceiveQr>, List<ReceiveQr>?> { it }.onStart { emit(null) },
        selectedIndex,
        loadError,
    ) { codes, index, error ->
        when {
            codes == null && error != null -> ReceiveViewState.Error(error.toUserMessage(), error is BankError.NoInternet)
            codes == null -> ReceiveViewState.Loading
            codes.isEmpty() -> ReceiveViewState.Empty
            else -> ReceiveViewState.Success(codes, index.coerceIn(codes.indices))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReceiveViewState.Loading)

    init {
        load()
    }

    fun onAction(action: ReceiveViewAction) {
        when (action) {
            is ReceiveViewAction.OnAccountSelected -> selectedIndex.value = action.index
            ReceiveViewAction.OnRetry -> load()
            ReceiveViewAction.OnShareClicked -> {
                val qr = (viewState.value as? ReceiveViewState.Success)?.selected ?: return
                _events.trySend(
                    ReceiveEvent.Share("Send money to ${qr.accountName}\nAccount: ${qr.accountNumber.formatAccountNumber()} (${qr.currency.name})")
                )
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            loadError.value = null
            refreshBankData().onFailure { loadError.value = it }
        }
    }
}
