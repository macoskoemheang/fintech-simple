package com.example.m3showcase.feature.transfer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m3showcase.core.connectivity.NetworkStatus
import com.example.m3showcase.core.domain.model.Account
import com.example.m3showcase.core.domain.model.BankError
import com.example.m3showcase.core.domain.model.TransferRequest
import com.example.m3showcase.core.domain.usecase.ObserveAccountsUseCase
import com.example.m3showcase.core.domain.usecase.ObserveNetworkStatusUseCase
import com.example.m3showcase.core.ui.toUserMessage
import com.example.m3showcase.feature.transfer.domain.usecase.MakeTransferUseCase
import com.example.m3showcase.feature.transfer.domain.usecase.ValidateTransferUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.m3showcase.core.ui.UiText

class TransferViewModel(
    initialFromAccountId: String?,
    observeAccounts: ObserveAccountsUseCase,
    observeNetworkStatus: ObserveNetworkStatusUseCase,
    private val validateTransfer: ValidateTransferUseCase,
    private val makeTransfer: MakeTransferUseCase,
) : ViewModel() {

    /** What the user typed. Kept apart from the data streams it is combined with. */
    private data class FormInput(
        val selectedAccountId: String? = null,
        val recipient: String = "",
        val amount: String = "",
        val note: String = "",
        val recipientError: UiText? = null,
        val amountError: UiText? = null,
        val isSubmitting: Boolean = false,
        val pendingConfirmation: TransferRequest? = null,
    )

    private val input = MutableStateFlow(FormInput(selectedAccountId = initialFromAccountId))
    private var latestAccounts: List<Account> = emptyList()

    private val _events = Channel<TransferEvent>(Channel.BUFFERED)
    val events: Flow<TransferEvent> = _events.receiveAsFlow()

    /**
     * Three streams → one UI state:
     *  - accounts (cold, from the cache): balances update right after a transfer
     *  - network status (hot, app-wide StateFlow): disables Continue while offline
     *  - form input (hot, local MutableStateFlow)
     */
    val viewState: StateFlow<TransferViewState> = combine(
        observeAccounts(),
        observeNetworkStatus(),
        input,
    ) { accounts, network, form ->
        latestAccounts = accounts
        if (accounts.isEmpty()) return@combine TransferViewState.NoAccounts
        val selected = accounts.firstOrNull { it.id == form.selectedAccountId }
            ?: accounts.firstOrNull { it.isDefault } ?: accounts.first()
        TransferViewState.Form(
            accounts = accounts,
            selectedAccount = selected,
            recipient = form.recipient,
            amount = form.amount,
            note = form.note,
            recipientError = form.recipientError,
            amountError = form.amountError,
            isOnline = network == NetworkStatus.Available,
            isSubmitting = form.isSubmitting,
            pendingConfirmation = form.pendingConfirmation,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransferViewState.Loading)

    fun onAction(action: TransferViewAction) {
        when (action) {
            is TransferViewAction.OnAccountSelected ->
                input.update { it.copy(selectedAccountId = action.accountId, amountError = null) }
            is TransferViewAction.OnRecipientChanged ->
                input.update { it.copy(recipient = action.value.filter(Char::isDigit).take(9), recipientError = null) }
            is TransferViewAction.OnAmountChanged ->
                input.update { it.copy(amount = sanitizeAmount(action.value), amountError = null) }
            is TransferViewAction.OnQuickAmountSelected ->
                input.update { it.copy(amount = action.value, amountError = null) }
            is TransferViewAction.OnNoteChanged ->
                input.update { it.copy(note = action.value.take(ValidateTransferUseCase.MAX_NOTE_LENGTH)) }
            TransferViewAction.OnContinueClicked -> validate()
            TransferViewAction.OnConfirmClicked -> submit()
            TransferViewAction.OnConfirmDismissed -> input.update { it.copy(pendingConfirmation = null) }
            TransferViewAction.OnBackClicked -> _events.trySend(TransferEvent.NavigateBack)
        }
    }

    private fun validate() {
        val form = viewState.value as? TransferViewState.Form ?: return
        validateTransfer(form.selectedAccount, form.recipient, form.amount, form.note)
            .onSuccess { request -> input.update { it.copy(pendingConfirmation = request) } }
            .onFailure { error -> applyError(error) }
    }

    private fun submit() {
        val request = input.value.pendingConfirmation ?: return
        if (input.value.isSubmitting) return // Guard against double taps, which would send two transfers.
        viewModelScope.launch {
            input.update { it.copy(isSubmitting = true) }
            makeTransfer(request)
                .onSuccess { tx ->
                    input.value = FormInput(selectedAccountId = request.fromAccountId)
                    _events.send(TransferEvent.OpenReceipt(tx.id))
                }
                .onFailure { error ->
                    input.update { it.copy(isSubmitting = false, pendingConfirmation = null) }
                    applyError(error)
                }
        }
    }

    private fun applyError(error: Throwable) {
        when {
            error is BankError.Validation && error.field == BankError.Field.AccountNumber ->
                input.update { it.copy(recipientError = error.toUserMessage()) }
            error is BankError.Validation && error.field == BankError.Field.Amount ->
                input.update { it.copy(amountError = error.toUserMessage()) }
            error is BankError.InsufficientFunds ->
                input.update { it.copy(amountError = error.toUserMessage()) }
            else -> _events.trySend(TransferEvent.ShowMessage(error.toUserMessage()))
        }
    }

    /** Keeps digits and at most one decimal point; validation does the rest. */
    private fun sanitizeAmount(raw: String): String {
        val cleaned = raw.filter { it.isDigit() || it == '.' }
        val firstDot = cleaned.indexOf('.')
        return if (firstDot < 0) cleaned.take(12)
        else (cleaned.substring(0, firstDot + 1) + cleaned.substring(firstDot + 1).replace(".", "")).take(12)
    }
}
