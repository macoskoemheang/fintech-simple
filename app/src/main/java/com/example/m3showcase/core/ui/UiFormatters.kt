package com.example.m3showcase.core.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.ConfigurationCompat
import com.example.m3showcase.R
import com.example.m3showcase.core.domain.model.BankError
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Maps domain errors to user-facing text, resolved later in the user's language. */
fun Throwable.toUserMessage(): UiText = when (this) {
    BankError.NoInternet -> UiText.res(R.string.error_no_internet)
    BankError.Timeout -> UiText.res(R.string.error_timeout)
    BankError.InvalidPin -> UiText.res(R.string.error_invalid_pin)
    BankError.InsufficientFunds -> UiText.res(R.string.error_insufficient_funds)
    BankError.AccountNotFound -> UiText.res(R.string.error_account_not_found)
    BankError.NotFound -> UiText.res(R.string.error_not_found)
    is BankError.Validation -> toUserMessage()
    else -> UiText.res(R.string.error_generic)
}

private fun BankError.Validation.toUserMessage(): UiText = when (rule) {
    BankError.Rule.Required -> UiText.res(R.string.validation_choose_account)
    BankError.Rule.WrongLength -> when (field) {
        BankError.Field.Pin -> UiText.res(R.string.validation_pin_length, limit ?: 0)
        else -> UiText.res(R.string.validation_account_length, limit ?: 0)
    }
    BankError.Rule.SameAccount -> UiText.res(R.string.validation_same_account)
    BankError.Rule.CurrencyMismatch -> UiText.res(R.string.validation_currency_mismatch)
    BankError.Rule.NotANumber -> UiText.res(R.string.validation_amount_invalid)
    BankError.Rule.NotPositive -> UiText.res(R.string.validation_amount_positive)
    BankError.Rule.TooManyDecimals -> UiText.Plural(R.plurals.validation_amount_decimals, limit ?: 0)
    BankError.Rule.AboveLimit -> UiText.res(R.string.validation_amount_limit)
    BankError.Rule.InsufficientBalance -> UiText.res(R.string.validation_insufficient_balance)
}

/** Formats account numbers ABA-style: 000 123 456 */
fun String.formatAccountNumber(): String = chunked(3).joinToString(" ")

/** The locale the app is displaying, which may differ from the device locale. */
fun Context.appLocale(): Locale = ConfigurationCompat.getLocales(resources.configuration)[0] ?: Locale.getDefault()

/** Dates in the app's language: month names and AM/PM come from the locale, the words from resources. */
object DateFormats {
    fun relativeDay(context: Context, millis: Long, now: Long = System.currentTimeMillis()): String {
        val locale = context.appLocale()
        val then = Calendar.getInstance().apply { timeInMillis = millis }
        val today = Calendar.getInstance().apply { timeInMillis = now }
        val time = SimpleDateFormat("h:mm a", locale).format(Date(millis))
        val sameYear = then.get(Calendar.YEAR) == today.get(Calendar.YEAR)
        val dayDiff = today.get(Calendar.DAY_OF_YEAR) - then.get(Calendar.DAY_OF_YEAR)
        return when {
            sameYear && dayDiff == 0 -> context.getString(R.string.date_today_at, time)
            sameYear && dayDiff == 1 -> context.getString(R.string.date_yesterday_at, time)
            else -> SimpleDateFormat("dd MMM yyyy, h:mm a", locale).format(Date(millis))
        }
    }

    fun full(context: Context, millis: Long): String =
        SimpleDateFormat("dd MMM yyyy, h:mm:ss a", context.appLocale()).format(Date(millis))
}

@Composable
fun relativeDayText(millis: Long): String {
    LocalConfiguration.current // Re-format when the language changes.
    return DateFormats.relativeDay(LocalContext.current, millis)
}

@Composable
fun fullDateText(millis: Long): String {
    LocalConfiguration.current
    return DateFormats.full(LocalContext.current, millis)
}

/** "23 Nov 2026" in the app's language. */
@Composable
fun dateText(date: com.example.m3showcase.core.domain.model.CalendarDate): String {
    LocalConfiguration.current
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply { clear(); set(date.year, date.month - 1, date.day) }
    return SimpleDateFormat("d MMM yyyy", context.appLocale()).format(calendar.time)
}

/** "5.25%" from basis points (525). */
fun formatRate(basisPoints: Int): String = "%.2f%%".format(Locale.US, basisPoints / 100.0)

/** "8:30 AM" in the app's language, from minutes since midnight. */
@Composable
fun timeOfDayText(minuteOfDay: Int): String {
    LocalConfiguration.current
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply { clear(); set(Calendar.HOUR_OF_DAY, minuteOfDay / 60); set(Calendar.MINUTE, minuteOfDay % 60) }
    return SimpleDateFormat("h:mm a", context.appLocale()).format(calendar.time)
}
