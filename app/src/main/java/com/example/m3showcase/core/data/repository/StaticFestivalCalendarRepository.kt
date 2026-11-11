package com.example.m3showcase.core.data.repository

import com.example.m3showcase.core.domain.model.CalendarDate
import com.example.m3showcase.core.domain.model.EventTheme
import com.example.m3showcase.core.domain.model.FestivalWindow
import com.example.m3showcase.core.domain.repository.FestivalCalendarRepository
import java.util.Calendar

/**
 * Bundled festival dates.
 *
 * Khmer New Year and Pchum Ben / Water Festival follow the lunar calendar, and the
 * government confirms them by sub-decree each year. In production this table should come
 * from remote config so it can be updated without an app release. Years not listed here
 * still get Independence Day, which is always 9 November.
 */
class StaticFestivalCalendarRepository : FestivalCalendarRepository {

    override fun windowsFor(year: Int): List<FestivalWindow> {
        val lunar = LunarHolidays[year].orEmpty().map { (theme, range) ->
            window(theme, range.first, range.second)
        }
        val independence = CalendarDate(year, 11, 9).let { window(EventTheme.IndependenceDay, it, it) }
        return (lunar + independence).sortedBy { it.showFrom }
    }

    private fun window(theme: EventTheme, start: CalendarDate, end: CalendarDate) = FestivalWindow(
        theme = theme,
        holidayStart = start,
        holidayEnd = end,
        showFrom = start.minusDays(LeadDays.getValue(theme)),
    )

    private companion object {
        /** How many days before the holiday the theme switches on. */
        val LeadDays = mapOf(
            EventTheme.WaterFestival to 5,
            EventTheme.KhmerNewYear to 7,
            // Pchum Ben is a 15-day period that ends with the 3-day public holiday.
            EventTheme.PchumBen to 12,
            EventTheme.IndependenceDay to 2,
        )

        // Public-holiday days from the Royal Government sub-decrees. 2027 is provisional.
        val LunarHolidays: Map<Int, List<Pair<EventTheme, Pair<CalendarDate, CalendarDate>>>> = mapOf(
            2025 to listOf(
                EventTheme.KhmerNewYear to (d(2025, 4, 14) to d(2025, 4, 16)),
                EventTheme.PchumBen to (d(2025, 9, 21) to d(2025, 9, 23)),
                EventTheme.WaterFestival to (d(2025, 11, 4) to d(2025, 11, 6)),
            ),
            2026 to listOf(
                EventTheme.KhmerNewYear to (d(2026, 4, 14) to d(2026, 4, 16)),
                EventTheme.PchumBen to (d(2026, 10, 10) to d(2026, 10, 12)),
                EventTheme.WaterFestival to (d(2026, 11, 23) to d(2026, 11, 25)),
            ),
            2027 to listOf(
                EventTheme.KhmerNewYear to (d(2027, 4, 14) to d(2027, 4, 16)),
                EventTheme.PchumBen to (d(2027, 9, 29) to d(2027, 10, 1)),
                EventTheme.WaterFestival to (d(2027, 11, 12) to d(2027, 11, 14)),
            ),
        )

        fun d(year: Int, month: Int, day: Int) = CalendarDate(year, month, day)
    }
}

private fun CalendarDate.minusDays(days: Int): CalendarDate {
    val cal = Calendar.getInstance().apply {
        clear()
        set(year, month - 1, day)
        add(Calendar.DAY_OF_MONTH, -days)
    }
    return CalendarDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
}
