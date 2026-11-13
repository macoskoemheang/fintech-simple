package com.example.m3showcase.feature.receive.presentation.detail

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m3showcase.R
import com.example.m3showcase.core.domain.model.BankError
import com.example.m3showcase.core.ui.Loadable
import com.example.m3showcase.core.ui.UiText
import com.example.m3showcase.core.ui.formatAccountNumber
import com.example.m3showcase.core.ui.toUserMessage
import com.example.m3showcase.core.common.MoneyFormatter
import com.example.m3showcase.feature.receive.domain.usecase.BuildReceiveRequestUseCase
import com.example.m3showcase.feature.receive.domain.usecase.ObserveReceiveQrUseCase
import com.example.m3showcase.feature.receive.domain.usecase.ReceiveRequest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

@Immutable
data class ReceiveQrDetailViewState(
    /** The last valid request; kept while the user is mid-way through typing an invalid amount. */
    val request: Loadable<ReceiveRequest> = Loadable.Loading,
    val amountText: String = "",
    val note: String = "",
    val amountError: UiText? = null,
)

sealed interface ReceiveQrDetailViewAction {
    data class OnAmountChanged(val value: String) : ReceiveQrDetailViewAction
    data class OnQuickAmount(val value: String) : ReceiveQrDetailViewAction
    data class OnNoteChanged(val value: String) : ReceiveQrDetailViewAction
    data object OnCopyClicked : ReceiveQrDetailViewAction
    data object OnShareClicked : ReceiveQrDetailViewAction
    data object OnBackClicked : ReceiveQrDetailViewAction
}

sealed interface ReceiveQrDetailEvent {
    data class Copy(val accountNumber: String) : ReceiveQrDetailEvent
    data class Share(val text: UiText) : ReceiveQrDetailEvent
    data object NavigateBack : ReceiveQrDetailEvent
}

class ReceiveQrDetailViewModel(
    private val accountNumber: String,
    observeReceiveQr: ObserveReceiveQrUseCase,
    private val buildRequest: BuildReceiveRequestUseCase,
) : ViewModel() {

    private val amountText = MutableStateFlow("")
    private val note = MutableStateFlow("")
    private var lastValid: ReceiveRequest? = null

    private val _events = Channel<ReceiveQrDetailEvent>(Channel.BUFFERED)
    val events: Flow<ReceiveQrDetailEvent> = _events.receiveAsFlow()

    private val qr = observeReceiveQr().map { codes -> codes.firstOrNull { it.accountNumber == accountNumber } }

    val viewState: StateFlow<ReceiveQrDetailViewState> = combine(qr, amountText, note) { qr, amount, note ->
        if (qr == null) {
            return@combine ReceiveQrDetailViewState(Loadable.Error(BankError.AccountNotFound.toUserMessage(), isOffline = false), amount, note)
        }
        buildRequest(qr, amount, note).fold(
            onSuccess = { request ->
                lastValid = request
                ReceiveQrDetailViewState(Loadable.Success(request), amount, note)
            },
            onFailure = { error ->
                val fallback = lastValid?.takeIf { it.qr == qr } ?: buildRequest(qr, "", note).getOrThrow()
                ReceiveQrDetailViewState(Loadable.Success(fallback), amount, note, amountError = error.toUserMessage())
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReceiveQrDetailViewState())

    fun onAction(action: ReceiveQrDetailViewAction) {
        when (action) {
            is ReceiveQrDetailViewAction.OnAmountChanged -> amountText.value = action.value.filter { it.isDigit() || it == '.' }.take(12)
            is ReceiveQrDetailViewAction.OnQuickAmount -> amountText.value = action.value
            is ReceiveQrDetailViewAction.OnNoteChanged -> note.value = action.value.take(BuildReceiveRequestUseCase.MAX_NOTE_LENGTH)
            ReceiveQrDetailViewAction.OnCopyClicked -> _events.trySend(ReceiveQrDetailEvent.Copy(accountNumber))
            ReceiveQrDetailViewAction.OnShareClicked -> shareText()?.let { _events.trySend(ReceiveQrDetailEvent.Share(it)) }
            ReceiveQrDetailViewAction.OnBackClicked -> _events.trySend(ReceiveQrDetailEvent.NavigateBack)
        }
    }

    private fun shareText(): UiText? {
        val state = viewState.value
        val request = (state.request as? Loadable.Success)?.data ?: return null
        val qr = request.qr
        val number = qr.accountNumber.formatAccountNumber()
        // Don't share an amount the user is still fixing.
        val amount = request.amount.takeIf { state.amountError == null }
        return if (amount == null) UiText.res(R.string.receive_share_text, qr.accountName, number, qr.currency.name)
        else UiText.res(R.string.receive_share_text_amount, qr.accountName, number, qr.currency.name, MoneyFormatter.format(amount))
    }
}
