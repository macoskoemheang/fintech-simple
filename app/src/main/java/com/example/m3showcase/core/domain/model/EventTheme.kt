package com.example.m3showcase.core.domain.model

/**
 * Festival skins, like the seasonal themes in Cambodian banking apps.
 * They change the brand colors and the hero artwork; light/dark is chosen separately.
 */
enum class EventTheme {
    /** Uses the festival in season, or [Default] when none is. Never applied as-is. */
    Auto,
    Default,
    WaterFestival,
    KhmerNewYear,
    PchumBen,
    IndependenceDay,
    /** Seasonal looks. Picked by hand only; Auto never switches to them. */
    Winter,
    Spring,
    ;

    val isFestival: Boolean get() = this != Auto && this != Default

    companion object {
        /** Themes a user can pick explicitly, in display order. */
        val festivals: List<EventTheme> = listOf(
            WaterFestival, KhmerNewYear, PchumBen, IndependenceDay, Winter, Spring,
        )
    }
}

/** A calendar day with no time zone. java.time needs API 26 and minSdk is 24. */
data class CalendarDate(val year: Int, val month: Int, val day: Int) : Comparable<CalendarDate> {
    init {
        require(month in 1..12 && day in 1..31) { "Invalid date $year-$month-$day" }
    }

    private val sortKey: Int get() = year * 10_000 + month * 100 + day

    override fun compareTo(other: CalendarDate): Int = sortKey.compareTo(other.sortKey)
}

/**
 * When a festival theme is shown automatically.
 * [showFrom] is a few days before the holiday, so the app gets festive in the run-up.
 */
data class FestivalWindow(
    val theme: EventTheme,
    /** First public-holiday day, used for labels like "23–25 Nov". */
    val holidayStart: CalendarDate,
    val holidayEnd: CalendarDate,
    val showFrom: CalendarDate,
) {
    init {
        require(theme.isFestival) { "$theme is not a festival" }
        require(showFrom <= holidayStart && holidayStart <= holidayEnd) { "Bad window for $theme" }
    }

    operator fun contains(date: CalendarDate): Boolean = date in showFrom..holidayEnd
}
