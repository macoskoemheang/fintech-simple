package com.example.m3showcase.core.domain.model

/**
 * Every failure the domain layer can report. The data layer maps low-level
 * exceptions (IOException, HTTP codes, …) into these so the UI never sees them.
 */
sealed class BankError(message: String) : Exception(message) {
    data object NoInternet : BankError("No internet connection")
    data object Timeout : BankError("The request timed out")
    data object InvalidPin : BankError("Incorrect PIN")
    data object InsufficientFunds : BankError("Insufficient funds")
    data object AccountNotFound : BankError("Account not found")
    data object NotFound : BankError("Not found")
    /**
     * A broken input rule. It carries a [rule] code, not text: wording and translation belong to the UI.
     * [limit] is the number the rule talks about (digits, decimal places), when there is one.
     */
    data class Validation(val field: Field, val rule: Rule, val limit: Int? = null) : BankError("$field: $rule")
    data class Unknown(val causeMessage: String?) : BankError(causeMessage ?: "Something went wrong")

    enum class Field { Pin, AccountNumber, Amount, FromAccount }

    enum class Rule {
        Required, WrongLength, SameAccount, CurrencyMismatch,
        NotANumber, NotPositive, TooManyDecimals, AboveLimit, InsufficientBalance,
    }
}

/** Unwraps any throwable into a [BankError]. */
fun Throwable.asBankError(): BankError = this as? BankError ?: BankError.Unknown(message)
