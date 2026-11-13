package com.example.m3showcase.feature.services

import com.example.m3showcase.core.domain.model.CalendarDate
import com.example.m3showcase.core.domain.model.calendarDateOfEpochDay
import com.example.m3showcase.core.domain.model.daysUntil
import com.example.m3showcase.core.domain.model.monthsUntil
import com.example.m3showcase.core.domain.model.plusDays
import com.example.m3showcase.core.domain.model.plusMonths
import com.example.m3showcase.core.domain.model.toEpochDay
import org.junit.Assert.assertEquals
import org.junit.Test

class CalendarDateMathTest {
    @Test fun `epoch day matches known values`() {
        assertEquals(0L, CalendarDate(1970, 1, 1).toEpochDay())
        assertEquals(20_405L, CalendarDate(2025, 11, 13).toEpochDay())
        assertEquals(CalendarDate(2024, 2, 29), calendarDateOfEpochDay(CalendarDate(2024, 2, 29).toEpochDay()))
    }

    @Test fun `adding months clamps to the end of the month`() {
        assertEquals(CalendarDate(2025, 2, 28), CalendarDate(2025, 1, 31).plusMonths(1))
        assertEquals(CalendarDate(2024, 2, 29), CalendarDate(2024, 1, 31).plusMonths(1))
        assertEquals(CalendarDate(2025, 12, 15), CalendarDate(2026, 1, 15).plusMonths(-1))
    }

    @Test fun `day and month differences`() {
        assertEquals(366L, CalendarDate(2024, 1, 1).daysUntil(CalendarDate(2025, 1, 1)))
        assertEquals(CalendarDate(2026, 3, 1), CalendarDate(2026, 2, 28).plusDays(1))
        assertEquals(2, CalendarDate(2026, 1, 15).monthsUntil(CalendarDate(2026, 3, 20)))
        assertEquals(1, CalendarDate(2026, 1, 15).monthsUntil(CalendarDate(2026, 3, 14)))
    }
}
