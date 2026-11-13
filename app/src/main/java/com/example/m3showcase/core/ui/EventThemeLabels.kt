package com.example.m3showcase.core.ui

import androidx.annotation.StringRes
import com.example.m3showcase.R
import com.example.m3showcase.core.domain.model.CalendarDate
import com.example.m3showcase.core.domain.model.EventTheme
import com.example.m3showcase.core.domain.model.FestivalWindow
import com.example.m3showcase.core.domain.model.SessionTimeout
import com.example.m3showcase.core.domain.model.ThemeMode
import java.text.DateFormatSymbols
import java.util.Locale

@get:StringRes
val EventTheme.nameRes: Int
    get() = when (this) {
        EventTheme.Auto -> R.string.event_auto
        EventTheme.Default -> R.string.event_classic
        EventTheme.WaterFestival -> R.string.event_water_festival
        EventTheme.KhmerNewYear -> R.string.event_khmer_new_year
        EventTheme.PchumBen -> R.string.event_pchum_ben
        EventTheme.IndependenceDay -> R.string.event_independence_day
        EventTheme.Winter -> R.string.event_winter
        EventTheme.Spring -> R.string.event_spring
    }

/** Khmer name shown under the translated one (hidden when the app itself is in Khmer). */
val EventTheme.khmerName: String?
    get() = when (this) {
        EventTheme.WaterFestival -> "បុណ្យអុំទូក"
        EventTheme.KhmerNewYear -> "ចូលឆ្នាំខ្មែរ"
        EventTheme.PchumBen -> "បុណ្យភ្ជុំបិណ្ឌ"
        EventTheme.IndependenceDay -> "ទិវាឯករាជ្យ"
        EventTheme.Winter -> "រដូវរងា"
        EventTheme.Spring -> "រដូវផ្ការីក"
        EventTheme.Auto, EventTheme.Default -> null
    }

/** Replaces "Good morning" on Home while a festival theme is on. Pchum Ben is a remembrance, so it's gentle. */
@get:StringRes
val EventTheme.greetingRes: Int?
    get() = when (this) {
        EventTheme.WaterFestival -> R.string.greeting_water_festival
        EventTheme.KhmerNewYear -> R.string.greeting_khmer_new_year
        EventTheme.PchumBen -> R.string.greeting_pchum_ben
        EventTheme.IndependenceDay -> R.string.greeting_independence_day
        EventTheme.Winter -> R.string.greeting_winter
        EventTheme.Spring -> R.string.greeting_spring
        EventTheme.Auto, EventTheme.Default -> null
    }

private fun CalendarDate.short(locale: Locale) = "$day ${DateFormatSymbols.getInstance(locale).shortMonths[month - 1]}"

/** "23–25 Nov 2026", "29 Sep – 1 Oct 2027" or "9 Nov 2026", with month names in [locale]. */
fun FestivalWindow.holidayLabel(locale: Locale): String = when {
    holidayStart == holidayEnd -> "${holidayStart.short(locale)} ${holidayStart.year}"
    holidayStart.month == holidayEnd.month -> "${holidayStart.day}–${holidayEnd.short(locale)} ${holidayEnd.year}"
    else -> "${holidayStart.short(locale)} – ${holidayEnd.short(locale)} ${holidayEnd.year}"
}

@get:StringRes
val SessionTimeout.labelRes: Int
    get() = when (this) {
        SessionTimeout.FiveSeconds -> R.string.timeout_5_seconds
        SessionTimeout.FiveMinutes -> R.string.timeout_5_minutes
        SessionTimeout.TenMinutes -> R.string.timeout_10_minutes
        SessionTimeout.FifteenMinutes -> R.string.timeout_15_minutes
    }

@get:StringRes
val ThemeMode.labelRes: Int
    get() = when (this) {
        ThemeMode.System -> R.string.theme_mode_system
        ThemeMode.Light -> R.string.theme_mode_light
        ThemeMode.Dark -> R.string.theme_mode_dark
    }
