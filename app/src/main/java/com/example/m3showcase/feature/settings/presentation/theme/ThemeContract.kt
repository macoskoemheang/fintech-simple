package com.example.m3showcase.feature.settings.presentation.theme

import androidx.compose.runtime.Immutable
import com.example.m3showcase.core.domain.model.EventTheme
import com.example.m3showcase.core.domain.model.ThemeMode
import com.example.m3showcase.core.domain.model.FestivalWindow

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
        /** The next scheduled festival, for the Auto row. Null when nothing is scheduled. */
        val nextFestival: FestivalWindow?,
    ) : ThemeViewState
}

@Immutable
data class EventThemeOption(
    val theme: EventTheme,
    /** Next holiday dates. Null for Classic or unknown years. The UI formats them in the app's language. */
    val window: FestivalWindow?,
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
