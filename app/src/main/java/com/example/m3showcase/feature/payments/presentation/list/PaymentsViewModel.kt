package com.example.m3showcase.feature.payments.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m3showcase.core.paging.Paginator
import com.example.m3showcase.core.ui.Loadable
import com.example.m3showcase.core.ui.toLoadable
import com.example.m3showcase.core.ui.toUserMessage
import com.example.m3showcase.feature.payments.domain.model.BillerCategory
import com.example.m3showcase.feature.payments.domain.model.Payment
import com.example.m3showcase.feature.payments.domain.model.PaymentSummary
import com.example.m3showcase.feature.payments.domain.usecase.GetPaymentSummaryUseCase
import com.example.m3showcase.feature.payments.domain.usecase.GetPaymentsPageUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PaymentsViewModel(
    private val getPaymentsPage: GetPaymentsPageUseCase,
    private val getPaymentSummary: GetPaymentSummaryUseCase,
) : ViewModel() {

    private val category = MutableStateFlow<BillerCategory?>(null)
    private val summary = MutableStateFlow<Loadable<PaymentSummary>>(Loadable.Loading)

    private val _events = Channel<PaymentsEvent>(Channel.BUFFERED)
    val events: Flow<PaymentsEvent> = _events.receiveAsFlow()

    private val paginator = Paginator(viewModelScope, pageSize = 15, keyOf = Payment::id) { request ->
        getPaymentsPage(category.value, request)
    }

    val viewState: StateFlow<PaymentsViewState> = combine(category, paginator.state, summary, ::PaymentsViewState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PaymentsViewState())

    init {
        paginator.start()
        loadSummary()
        viewModelScope.launch { paginator.refreshErrors.collect { _events.send(PaymentsEvent.ShowMessage(it.toUserMessage())) } }
    }

    fun onAction(action: PaymentsViewAction) {
        when (action) {
            PaymentsViewAction.OnRefresh -> {
                paginator.refresh()
                loadSummary()
            }
            PaymentsViewAction.OnLoadMore -> paginator.loadNext()
            PaymentsViewAction.OnRetry -> {
                paginator.retry()
                if (summary.value is Loadable.Error) loadSummary()
            }
            is PaymentsViewAction.OnCategorySelected -> if (action.category != category.value) {
                category.value = action.category
                paginator.reset()
            }
            is PaymentsViewAction.OnPaymentClicked -> _events.trySend(PaymentsEvent.OpenPayment(action.id))
            PaymentsViewAction.OnBackClicked -> _events.trySend(PaymentsEvent.NavigateBack)
        }
    }

    private fun loadSummary() {
        viewModelScope.launch {
            val hadData = summary.value is Loadable.Success
            if (!hadData) summary.value = Loadable.Loading
            val result = getPaymentSummary().toLoadable()
            // A failed refresh keeps the last numbers; the list already reports the error.
            if (result is Loadable.Success || !hadData) summary.value = result
        }
    }
}
