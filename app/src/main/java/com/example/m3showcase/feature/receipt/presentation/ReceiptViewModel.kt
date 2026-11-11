package com.example.m3showcase.feature.receipt.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m3showcase.core.common.MoneyFormatter
import com.example.m3showcase.core.domain.usecase.GetTransactionUseCase
import com.example.m3showcase.core.ui.DateFormats
import com.example.m3showcase.core.ui.toUserMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ReceiptViewModel(
    private val transactionId: String,
    private val isNewTransfer: Boolean,
    private val getTransaction: GetTransactionUseCase,
) : ViewModel() {

    private val _viewState = MutableStateFlow<ReceiptViewState>(ReceiptViewState.Loading)
    val viewState: StateFlow<ReceiptViewState> = _viewState.asStateFlow()

    private val _events = Channel<ReceiptEvent>(Channel.BUFFERED)
    val events: Flow<ReceiptEvent> = _events.receiveAsFlow()

    init {
        load()
    }

    fun onAction(action: ReceiptViewAction) {
        when (action) {
            ReceiptViewAction.OnDoneClicked -> _events.trySend(ReceiptEvent.Close)
            ReceiptViewAction.OnRetry -> load()
            ReceiptViewAction.OnShareClicked -> {
                val tx = (_viewState.value as? ReceiptViewState.Success)?.transaction ?: return
                _events.trySend(
                    ReceiptEvent.Share(
                        "${tx.title}\n${MoneyFormatter.format(tx.amount, withSign = true)}\n" +
                            "${DateFormats.full(tx.timestampMillis)}\nRef: ${tx.id.take(12).uppercase()}"
                    )
                )
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _viewState.value = ReceiptViewState.Loading
            _viewState.value = getTransaction(transactionId).fold(
                onSuccess = { ReceiptViewState.Success(it, isNewTransfer) },
                onFailure = { ReceiptViewState.Error(it.toUserMessage()) },
            )
        }
    }
}
