package com.example.m3showcase.core.ui

import com.example.m3showcase.core.domain.model.CalendarDate
import com.example.m3showcase.core.domain.model.EventTheme
import com.example.m3showcase.core.domain.model.FestivalWindow

val EventTheme.displayName: String
    get() = when (this) {
        EventTheme.Auto -> "Auto"
        EventTheme.Default -> "Classic"
        EventTheme.WaterFestival -> "Water Festival"
        EventTheme.KhmerNewYear -> "Khmer New Year"
        EventTheme.PchumBen -> "Pchum Ben"
        EventTheme.IndependenceDay -> "Independence Day"
        EventTheme.Winter -> "Winter"
        EventTheme.Spring -> "Spring"
    }

/** Khmer name shown under the English one. */
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
val EventTheme.greeting: String?
    get() = when (this) {
        EventTheme.WaterFestival -> "Happy Water Festival"
        EventTheme.KhmerNewYear -> "Happy Khmer New Year"
        EventTheme.PchumBen -> "Peaceful Pchum Ben"
        EventTheme.IndependenceDay -> "Happy Independence Day"
        EventTheme.Winter -> "Stay warm this winter"
        EventTheme.Spring -> "Happy spring"
        EventTheme.Auto, EventTheme.Default -> null
    }

private val MonthShort = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

private fun CalendarDate.short() = "$day ${MonthShort[month - 1]}"

/** "23–25 Nov 2026", "29 Sep – 1 Oct 2027" or "9 Nov 2026". */
fun FestivalWindow.holidayLabel(): String = when {
    holidayStart == holidayEnd -> "${holidayStart.short()} ${holidayStart.year}"
    holidayStart.month == holidayEnd.month -> "${holidayStart.day}–${holidayEnd.short()} ${holidayEnd.year}"
    else -> "${holidayStart.short()} – ${holidayEnd.short()} ${holidayEnd.year}"
}
