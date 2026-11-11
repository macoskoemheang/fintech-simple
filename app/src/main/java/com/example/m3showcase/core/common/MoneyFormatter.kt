package com.example.m3showcase.core.common

import com.example.m3showcase.core.domain.model.CurrencyCode
import com.example.m3showcase.core.domain.model.Money
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/** Formats [Money] for display. Lives in common because both UI and use cases need it. */
object MoneyFormatter {

    fun format(money: Money, withSign: Boolean = false): String {
        val pattern = if (money.currency.fractionDigits == 0) "#,##0" else "#,##0.00"
        val formatter = DecimalFormat(pattern, DecimalFormatSymbols(Locale.US))
        val value = BigDecimal.valueOf(money.minor).movePointLeft(money.currency.fractionDigits).abs()
        val sign = when {
            !withSign -> if (money.minor < 0) "-" else ""
            money.minor < 0 -> "- "
            else -> "+ "
        }
        val body = formatter.format(value)
        return when (money.currency) {
            CurrencyCode.USD -> "$sign${money.currency.symbol}$body"
            CurrencyCode.KHR -> "$sign$body ${money.currency.symbol}"
        }
    }

    /** Masked form used when the user hides balances. */
    fun masked(currency: CurrencyCode): String = when (currency) {
        CurrencyCode.USD -> "${currency.symbol} ••••••"
        CurrencyCode.KHR -> "•••••• ${currency.symbol}"
    }
}
