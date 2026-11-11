package com.example.m3showcase.feature.transfer.presentation

import androidx.compose.runtime.Immutable
import com.example.m3showcase.core.domain.model.Account
import com.example.m3showcase.core.domain.model.TransferRequest

@Immutable
sealed interface TransferViewState {
    data object Loading : TransferViewState
    data object NoAccounts : TransferViewState

    data class Form(
        val accounts: List<Account>,
        val selectedAccount: Account?,
        val recipient: String,
        val amount: String,
        val note: String,
        val recipientError: String?,
        val amountError: String?,
        val isOnline: Boolean,
        val isSubmitting: Boolean,
        /** Non-null shows the confirmation bottom sheet. */
        val pendingConfirmation: TransferRequest?,
    ) : TransferViewState {
        val canContinue: Boolean get() = isOnline && !isSubmitting && recipient.isNotBlank() && amount.isNotBlank()
    }
}

sealed interface TransferViewAction {
    data class OnAccountSelected(val accountId: String) : TransferViewAction
    data class OnRecipientChanged(val value: String) : TransferViewAction
    data class OnAmountChanged(val value: String) : TransferViewAction
    data class OnQuickAmountSelected(val value: String) : TransferViewAction
    data class OnNoteChanged(val value: String) : TransferViewAction
    data object OnContinueClicked : TransferViewAction
    data object OnConfirmClicked : TransferViewAction
    data object OnConfirmDismissed : TransferViewAction
    data object OnBackClicked : TransferViewAction
}

sealed interface TransferEvent {
    data class ShowMessage(val text: String) : TransferEvent
    data class OpenReceipt(val transactionId: String) : TransferEvent
    data object NavigateBack : TransferEvent
}
