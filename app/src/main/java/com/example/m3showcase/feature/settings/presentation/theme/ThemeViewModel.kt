package com.example.m3showcase.feature.settings.presentation.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m3showcase.core.domain.model.EventTheme
import com.example.m3showcase.core.domain.usecase.GetActiveFestivalUseCase
import com.example.m3showcase.core.domain.usecase.GetFestivalScheduleUseCase
import com.example.m3showcase.core.domain.usecase.ObserveSettingsUseCase
import com.example.m3showcase.core.domain.usecase.ResolveEventThemeUseCase
import com.example.m3showcase.core.domain.usecase.SetEventThemeUseCase
import com.example.m3showcase.core.domain.usecase.SetThemeModeUseCase
import com.example.m3showcase.core.ui.displayName
import com.example.m3showcase.core.ui.holidayLabel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(
    observeSettings: ObserveSettingsUseCase,
    private val setThemeMode: SetThemeModeUseCase,
    private val setEventTheme: SetEventThemeUseCase,
    private val resolveEventTheme: ResolveEventThemeUseCase,
    getActiveFestival: GetActiveFestivalUseCase,
    getFestivalSchedule: GetFestivalScheduleUseCase,
) : ViewModel() {

    private val _events = Channel<ThemeEvent>(Channel.BUFFERED)
    val events: Flow<ThemeEvent> = _events.receiveAsFlow()

    // The schedule only changes by date, so it's read once per screen visit.
    private val live = getActiveFestival()?.theme
    private val schedule = getFestivalSchedule()
    private val options = listOf(EventThemeOption(EventTheme.Default, dateLabel = null, isLive = false)) +
        EventTheme.festivals.map { theme ->
            EventThemeOption(theme, schedule[theme]?.holidayLabel(), isLive = theme == live)
        }
    private val nextLabel = schedule.values.minByOrNull { it.showFrom }
        ?.let { "${it.theme.displayName} · ${it.holidayLabel()}" }

    // Cold DataStore flow → hot StateFlow. The selection on screen always shows what
    // is saved, so it can't drift from the theme the app is actually using.
    val viewState: StateFlow<ThemeViewState> = observeSettings()
        .map { it.themeMode to it.eventTheme }
        .distinctUntilChanged()
        .map<_, ThemeViewState> { (mode, event) ->
            ThemeViewState.Success(
                selected = mode,
                isAutoEvent = event == EventTheme.Auto,
                appliedEvent = resolveEventTheme(event),
                eventOptions = options,
                nextFestivalLabel = nextLabel,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeViewState.Loading)

    fun onAction(action: ThemeViewAction) {
        when (action) {
            // Saving is all it takes: MainViewModel observes the same settings and re-themes the app.
            is ThemeViewAction.OnThemeSelected -> viewModelScope.launch { setThemeMode(action.mode) }
            // Tapping a card is an explicit choice, so it turns Auto off.
            is ThemeViewAction.OnEventThemeSelected -> viewModelScope.launch { setEventTheme(action.theme) }
            is ThemeViewAction.OnAutoEventToggled -> viewModelScope.launch {
                // Turning Auto off keeps whatever is on screen, so nothing jumps.
                val applied = (viewState.value as? ThemeViewState.Success)?.appliedEvent ?: EventTheme.Default
                setEventTheme(if (action.enabled) EventTheme.Auto else applied)
            }
            ThemeViewAction.OnBackClicked -> _events.trySend(ThemeEvent.NavigateBack)
        }
    }
}
