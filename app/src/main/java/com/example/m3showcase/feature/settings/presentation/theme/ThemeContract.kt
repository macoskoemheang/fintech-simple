package com.example.m3showcase.feature.settings.presentation.theme

import androidx.compose.runtime.Immutable
import com.example.m3showcase.core.domain.model.EventTheme
import com.example.m3showcase.core.domain.model.ThemeMode

@Immutable
sealed interface ThemeViewState {
    data object Loading : ThemeViewState
    data class Success(
        val selected: ThemeMode,
        /** True when the festival skin follows the calendar. */
        val isAutoEvent: Boolean,
        /** The skin on screen right now (Auto already resolved). */
        val appliedEvent: EventTheme,
        val eventOptions: List<EventThemeOption>,
        /** "Water Festival · 23–25 Nov 2026", for the Auto row. Null when nothing is scheduled. */
        val nextFestivalLabel: String?,
    ) : ThemeViewState
}

@Immutable
data class EventThemeOption(
    val theme: EventTheme,
    /** Next holiday dates, e.g. "23–25 Nov 2026". Null for Classic or unknown years. */
    val dateLabel: String?,
    /** In season today. */
    val isLive: Boolean,
)

sealed interface ThemeViewAction {
    data class OnThemeSelected(val mode: ThemeMode) : ThemeViewAction
    data class OnEventThemeSelected(val theme: EventTheme) : ThemeViewAction
    data class OnAutoEventToggled(val enabled: Boolean) : ThemeViewAction
    data object OnBackClicked : ThemeViewAction
}

sealed interface ThemeEvent {
    data object NavigateBack : ThemeEvent
}
