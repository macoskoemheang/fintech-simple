package com.example.m3showcase.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Navigation 3 keys. Each screen is a @Serializable NavKey, so the back stack
 * survives configuration changes and process death. Arguments are plain,
 * type-safe properties.
 */
sealed interface Destination : NavKey {

    @Serializable data object Login : Destination

    // Top-level (bottom bar) destinations
    @Serializable data object Home : Destination
    @Serializable data object Accounts : Destination
    @Serializable data object Receive : Destination
    @Serializable data object More : Destination

    // Detail destinations
    /** accountId = null shows transactions from all accounts. */
    @Serializable data class TransactionHistory(val accountId: String?) : Destination
    @Serializable data class Transfer(val fromAccountId: String? = null) : Destination
    @Serializable data class Receipt(val transactionId: String, val isNewTransfer: Boolean) : Destination
    @Serializable data object Notifications : Destination
    @Serializable data class ReceiveQrDetail(val accountNumber: String) : Destination
    @Serializable data object Payments : Destination
    @Serializable data class PaymentDetail(val paymentId: String) : Destination
    @Serializable data object Deposits : Destination
    @Serializable data class DepositDetail(val depositId: String) : Destination
    @Serializable data object Loans : Destination
    @Serializable data class LoanDetail(val loanId: String) : Destination
    @Serializable data object LoanReport : Destination
    @Serializable data object Locator : Destination
    @Serializable data object Theme : Destination
    @Serializable data object Showcase : Destination
}
