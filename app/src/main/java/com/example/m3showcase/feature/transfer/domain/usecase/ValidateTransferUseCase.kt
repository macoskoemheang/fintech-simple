package com.example.m3showcase.feature.transfer.domain.usecase

import com.example.m3showcase.core.domain.model.Account
import com.example.m3showcase.core.domain.model.BankError
import com.example.m3showcase.core.domain.model.BankError.Field
import com.example.m3showcase.core.domain.model.BankError.Rule
import com.example.m3showcase.core.domain.model.CurrencyCode
import com.example.m3showcase.core.domain.model.Money
import com.example.m3showcase.core.domain.model.TransferRequest
import java.math.BigDecimal

/** Pure business rules, no I/O, so it's simple to unit test. */
class ValidateTransferUseCase {

    operator fun invoke(
        from: Account?,
        toAccountNumber: String,
        amountText: String,
        note: String,
    ): Result<TransferRequest> {
        if (from == null) return invalid(Field.FromAccount, Rule.Required)

        val recipient = toAccountNumber.filter(Char::isDigit)
        if (recipient.length != ACCOUNT_NUMBER_LENGTH) {
            return invalid(Field.AccountNumber, Rule.WrongLength, ACCOUNT_NUMBER_LENGTH)
        }
        if (recipient == from.number) return invalid(Field.AccountNumber, Rule.SameAccount)

        val currency = from.balance.currency
        val decimal = amountText.toBigDecimalOrNull()
            ?: return invalid(Field.Amount, Rule.NotANumber)
        if (decimal.signum() <= 0) return invalid(Field.Amount, Rule.NotPositive)
        if (decimal.stripTrailingZeros().scale() > currency.fractionDigits) {
            return invalid(Field.Amount, Rule.TooManyDecimals, currency.fractionDigits)
        }
        val minor = decimal.movePointRight(currency.fractionDigits).toLong()
        if (minor > maxPerTransaction(currency)) return invalid(Field.Amount, Rule.AboveLimit)
        if (minor > from.balance.minor) return invalid(Field.Amount, Rule.InsufficientBalance)

        return Result.success(
            TransferRequest(
                fromAccountId = from.id,
                toAccountNumber = recipient,
                amount = Money(minor, currency),
                note = note.trim().take(MAX_NOTE_LENGTH).ifBlank { null },
            )
        )
    }

    private fun invalid(field: Field, rule: Rule, limit: Int? = null): Result<TransferRequest> =
        Result.failure(BankError.Validation(field, rule, limit))

    private fun maxPerTransaction(currency: CurrencyCode): Long = when (currency) {
        CurrencyCode.USD -> BigDecimal(10_000).movePointRight(2).toLong()
        CurrencyCode.KHR -> 40_000_000
    }

    companion object {
        const val ACCOUNT_NUMBER_LENGTH = 9
        const val MAX_NOTE_LENGTH = 100
    }
}
