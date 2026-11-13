package com.example.m3showcase.core

import com.example.m3showcase.core.data.repository.StaticFestivalCalendarRepository
import com.example.m3showcase.core.domain.model.CalendarDate
import com.example.m3showcase.core.domain.model.EventTheme
import com.example.m3showcase.core.domain.usecase.GetActiveFestivalUseCase
import com.example.m3showcase.core.domain.usecase.GetFestivalScheduleUseCase
import com.example.m3showcase.core.domain.usecase.ResolveEventThemeUseCase
import com.example.m3showcase.core.ui.holidayLabel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

class EventThemeUseCasesTest {

    private val calendar = StaticFestivalCalendarRepository()

    private fun resolverOn(date: CalendarDate) =
        ResolveEventThemeUseCase(GetActiveFestivalUseCase(calendar) { date })

    @Test
    fun `auto picks the water festival during the holiday`() {
        assertEquals(EventTheme.WaterFestival, resolverOn(CalendarDate(2026, 11, 24))(EventTheme.Auto))
    }

    @Test
    fun `auto switches on a few days early`() {
        // 2026 holiday is 23–25 Nov with a 5-day lead.
        assertEquals(EventTheme.WaterFestival, resolverOn(CalendarDate(2026, 11, 18))(EventTheme.Auto))
        assertEquals(EventTheme.Default, resolverOn(CalendarDate(2026, 11, 17))(EventTheme.Auto))
    }

    @Test
    fun `auto turns off the day after the holiday`() {
        assertEquals(EventTheme.Default, resolverOn(CalendarDate(2026, 11, 26))(EventTheme.Auto))
    }

    @Test
    fun `independence day works in years without lunar dates`() {
        assertEquals(EventTheme.IndependenceDay, resolverOn(CalendarDate(2031, 11, 9))(EventTheme.Auto))
    }

    @Test
    fun `explicit pick wins over the calendar`() {
        val resolve = resolverOn(CalendarDate(2026, 11, 24))
        assertEquals(EventTheme.KhmerNewYear, resolve(EventTheme.KhmerNewYear))
        assertEquals(EventTheme.Default, resolve(EventTheme.Default))
    }

    @Test
    fun `pchum ben lead crosses into the previous month`() {
        // 2027 holiday is 29 Sep – 1 Oct; 12-day lead starts 17 Sep.
        assertEquals(EventTheme.PchumBen, resolverOn(CalendarDate(2027, 9, 17))(EventTheme.Auto))
        assertNull(GetActiveFestivalUseCase(calendar) { CalendarDate(2027, 9, 16) }())
    }

    @Test
    fun `schedule rolls over to next year once a festival has passed`() {
        val schedule = GetFestivalScheduleUseCase(calendar) { CalendarDate(2026, 11, 11) }()
        assertEquals("23–25 Nov 2026", schedule.getValue(EventTheme.WaterFestival).holidayLabel(Locale.ENGLISH))
        assertEquals("14–16 Apr 2027", schedule.getValue(EventTheme.KhmerNewYear).holidayLabel(Locale.ENGLISH))
        assertEquals("29 Sep – 1 Oct 2027", schedule.getValue(EventTheme.PchumBen).holidayLabel(Locale.ENGLISH))
        assertEquals("9 Nov 2027", schedule.getValue(EventTheme.IndependenceDay).holidayLabel(Locale.ENGLISH))
    }
}
