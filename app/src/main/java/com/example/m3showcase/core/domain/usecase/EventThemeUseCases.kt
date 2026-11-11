package com.example.m3showcase.core.domain.usecase

import com.example.m3showcase.core.common.DateProvider
import com.example.m3showcase.core.domain.model.EventTheme
import com.example.m3showcase.core.domain.model.FestivalWindow
import com.example.m3showcase.core.domain.repository.FestivalCalendarRepository
import com.example.m3showcase.core.domain.repository.SettingsRepository

class SetEventThemeUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(theme: EventTheme) = repository.setEventTheme(theme)
}

/** The festival in season today, or null. */
class GetActiveFestivalUseCase(
    private val calendar: FestivalCalendarRepository,
    private val dates: DateProvider,
) {
    operator fun invoke(): FestivalWindow? {
        val today = dates.today()
        return calendar.windowsFor(today.year).firstOrNull { today in it }
    }
}

/**
 * Turns the saved choice into the theme to draw. Auto becomes today's festival or Default;
 * an explicit pick always wins, so users can keep a festival skin all year.
 */
class ResolveEventThemeUseCase(private val activeFestival: GetActiveFestivalUseCase) {
    operator fun invoke(selected: EventTheme): EventTheme = when (selected) {
        EventTheme.Auto -> activeFestival()?.theme ?: EventTheme.Default
        else -> selected
    }
}

/** Next (or current) dates for each festival, for labels on the theme picker. */
class GetFestivalScheduleUseCase(
    private val calendar: FestivalCalendarRepository,
    private val dates: DateProvider,
) {
    operator fun invoke(): Map<EventTheme, FestivalWindow> {
        val today = dates.today()
        val upcoming = (calendar.windowsFor(today.year) + calendar.windowsFor(today.year + 1))
            .filter { it.holidayEnd >= today }
        return EventTheme.festivals
            .mapNotNull { theme -> upcoming.firstOrNull { it.theme == theme }?.let { theme to it } }
            .toMap()
    }
}
