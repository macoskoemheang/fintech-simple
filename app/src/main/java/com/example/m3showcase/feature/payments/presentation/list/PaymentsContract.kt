package com.example.m3showcase.feature.payments.presentation.list

import androidx.compose.runtime.Immutable
import com.example.m3showcase.core.paging.PagingState
import com.example.m3showcase.core.ui.Loadable
import com.example.m3showcase.core.ui.UiText
import com.example.m3showcase.feature.payments.domain.model.BillerCategory
import com.example.m3showcase.feature.payments.domain.model.Payment
import com.example.m3showcase.feature.payments.domain.model.PaymentSummary

@Immutable
data class PaymentsViewState(
    /** null = all categories. */
    val category: BillerCategory? = null,
    val paging: PagingState<Payment> = PagingState(),
    val summary: Loadable<PaymentSummary> = Loadable.Loading,
)

sealed interface PaymentsViewAction {
    data object OnRefresh : PaymentsViewAction
    data object OnLoadMore : PaymentsViewAction
    data object OnRetry : PaymentsViewAction
    data class OnCategorySelected(val category: BillerCategory?) : PaymentsViewAction
    data class OnPaymentClicked(val id: String) : PaymentsViewAction
    data object OnBackClicked : PaymentsViewAction
}

sealed interface PaymentsEvent {
    data class ShowMessage(val text: UiText) : PaymentsEvent
    data class OpenPayment(val id: String) : PaymentsEvent
    data object NavigateBack : PaymentsEvent
}
