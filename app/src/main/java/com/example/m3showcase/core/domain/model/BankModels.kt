package com.example.m3showcase.core.domain.model

data class UserProfile(
    val id: String,
    val fullName: String,
    val phoneNumber: String,
)

data class Account(
    val id: String,
    val name: String,
    val number: String,
    val type: AccountType,
    val balance: Money,
    val isDefault: Boolean,
)

enum class AccountType { Savings, Current, FixedDeposit }

data class Transaction(
    val id: String,
    val accountId: String,
    val title: String,
    val counterparty: String,
    /** Signed: negative for money going out. */
    val amount: Money,
    val category: TransactionCategory,
    val timestampMillis: Long,
    val note: String? = null,
) {
    val isCredit: Boolean get() = amount.minor >= 0
}

enum class TransactionCategory { Transfer, Payment, TopUp, QrPayment, Salary, Cash }

data class TransferRequest(
    val fromAccountId: String,
    val toAccountNumber: String,
    val amount: Money,
    val note: String?,
)

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.System,
    val isBalanceHidden: Boolean = false,
    /** Festival skin on top of light/dark. [EventTheme.Auto] follows the festival calendar. */
    val eventTheme: EventTheme = EventTheme.Auto,
    /** Idle time before the app locks back to the PIN screen. */
    val sessionTimeout: SessionTimeout = SessionTimeout.Default,
)

enum class ThemeMode { System, Light, Dark }

/** What the Receive screen encodes into a QR code. */
data class ReceiveQr(
    val accountName: String,
    val accountNumber: String,
    val currency: CurrencyCode,
    val payload: String,
)
