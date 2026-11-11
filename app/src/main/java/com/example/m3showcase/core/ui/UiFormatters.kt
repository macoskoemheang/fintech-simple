package com.example.m3showcase.core.ui

import com.example.m3showcase.core.domain.model.BankError
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Maps domain errors to user-facing text. Move these to strings.xml to add Khmer. */
fun Throwable.toUserMessage(): String = when (this) {
    BankError.NoInternet -> "No internet connection. Check your network and try again."
    BankError.Timeout -> "The server took too long to respond. Please try again."
    BankError.InvalidPin -> "Incorrect PIN. Please try again."
    BankError.InsufficientFunds -> "Insufficient balance for this transfer."
    BankError.AccountNotFound -> "Account not found."
    BankError.NotFound -> "We couldn't find that item."
    is BankError.Validation -> reason
    else -> message ?: "Something went wrong. Please try again."
}

/** Formats account numbers ABA-style: 000 123 456 */
fun String.formatAccountNumber(): String = chunked(3).joinToString(" ")

object DateFormats {
    fun relativeDay(millis: Long, now: Long = System.currentTimeMillis()): String {
        val then = Calendar.getInstance().apply { timeInMillis = millis }
        val today = Calendar.getInstance().apply { timeInMillis = now }
        val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(millis))
        val sameYear = then.get(Calendar.YEAR) == today.get(Calendar.YEAR)
        val dayDiff = today.get(Calendar.DAY_OF_YEAR) - then.get(Calendar.DAY_OF_YEAR)
        return when {
            sameYear && dayDiff == 0 -> "Today, $time"
            sameYear && dayDiff == 1 -> "Yesterday, $time"
            else -> SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault()).format(Date(millis))
        }
    }

    fun full(millis: Long): String =
        SimpleDateFormat("dd MMM yyyy, h:mm:ss a", Locale.getDefault()).format(Date(millis))
}
