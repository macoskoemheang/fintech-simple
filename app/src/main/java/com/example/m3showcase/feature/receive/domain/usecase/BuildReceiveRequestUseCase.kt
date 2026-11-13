package com.example.m3showcase.feature.receive.domain.usecase

import com.example.m3showcase.core.domain.model.BankError
import com.example.m3showcase.core.domain.model.BankError.Field
import com.example.m3showcase.core.domain.model.BankError.Rule
import com.example.m3showcase.core.domain.model.CurrencyCode
import com.example.m3showcase.core.domain.model.Money
import com.example.m3showcase.core.domain.model.ReceiveQr
import java.math.BigDecimal
import java.net.URLEncoder

/** A QR the payer scans: the account, plus an optional fixed amount and note. */
data class ReceiveRequest(val qr: ReceiveQr, val amount: Money?, val note: String?, val payload: String)

/**
 * Adds an optional amount and note to an account's receive QR.
 * Empty amount = the payer types it (a "static" QR); otherwise it's validated like a transfer.
 */
class BuildReceiveRequestUseCase {

    operator fun invoke(qr: ReceiveQr, amountText: String, note: String): Result<ReceiveRequest> {
        val trimmedNote = note.trim().take(MAX_NOTE_LENGTH).ifBlank { null }
        val amount = if (amountText.isBlank()) null else parse(amountText, qr.currency).getOrElse { return Result.failure(it) }
        val payload = buildString {
            append(qr.payload)
            amount?.let { append("&amt=").append(BigDecimal.valueOf(it.minor).movePointLeft(qr.currency.fractionDigits).toPlainString()) }
            trimmedNote?.let { append("&note=").append(URLEncoder.encode(it, "UTF-8")) }
        }
        return Result.success(ReceiveRequest(qr, amount, trimmedNote, payload))
    }

    private fun parse(text: String, currency: CurrencyCode): Result<Money> {
        val decimal = text.toBigDecimalOrNull() ?: return invalid(Rule.NotANumber)
        if (decimal.signum() <= 0) return invalid(Rule.NotPositive)
        if (decimal.stripTrailingZeros().scale() > currency.fractionDigits) return invalid(Rule.TooManyDecimals, currency.fractionDigits)
        val minor = decimal.movePointRight(currency.fractionDigits).toLong()
        if (minor > maxRequest(currency)) return invalid(Rule.AboveLimit)
        return Result.success(Money(minor, currency))
    }

    private fun invalid(rule: Rule, limit: Int? = null): Result<Money> = Result.failure(BankError.Validation(Field.Amount, rule, limit))

    private fun maxRequest(currency: CurrencyCode): Long = when (currency) {
        CurrencyCode.USD -> 1_000_000 // $10,000.00
        CurrencyCode.KHR -> 40_000_000
    }

    companion object {
        const val MAX_NOTE_LENGTH = 50
    }
}
