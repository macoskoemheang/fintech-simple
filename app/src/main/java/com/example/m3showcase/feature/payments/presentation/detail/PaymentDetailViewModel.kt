package com.example.m3showcase.feature.payments.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m3showcase.core.ui.Loadable
import com.example.m3showcase.core.ui.toLoadable
import com.example.m3showcase.feature.payments.domain.model.Payment
import com.example.m3showcase.feature.payments.domain.usecase.GetPaymentUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed interface PaymentDetailViewAction {
    data object OnRetry : PaymentDetailViewAction
    data object OnShareClicked : PaymentDetailViewAction
    data object OnBackClicked : PaymentDetailViewAction
}

sealed interface PaymentDetailEvent {
    /** The UI words the receipt in the user's language. */
    data class Share(val payment: Payment) : PaymentDetailEvent
    data object NavigateBack : PaymentDetailEvent
}

class PaymentDetailViewModel(
    private val paymentId: String,
    private val getPayment: GetPaymentUseCase,
) : ViewModel() {

    private val _viewState = MutableStateFlow<Loadable<Payment>>(Loadable.Loading)
    val viewState: StateFlow<Loadable<Payment>> = _viewState.asStateFlow()

    private val _events = Channel<PaymentDetailEvent>(Channel.BUFFERED)
    val events: Flow<PaymentDetailEvent> = _events.receiveAsFlow()

    init {
        load()
    }

    fun onAction(action: PaymentDetailViewAction) {
        when (action) {
            PaymentDetailViewAction.OnRetry -> load()
            PaymentDetailViewAction.OnShareClicked ->
                (_viewState.value as? Loadable.Success)?.data?.let { _events.trySend(PaymentDetailEvent.Share(it)) }
            PaymentDetailViewAction.OnBackClicked -> _events.trySend(PaymentDetailEvent.NavigateBack)
        }
    }

    private fun load() {
        viewModelScope.launch {
            _viewState.value = Loadable.Loading
            _viewState.value = getPayment(paymentId).toLoadable()
        }
    }
}
