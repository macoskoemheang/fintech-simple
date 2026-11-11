package com.example.m3showcase.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m3showcase.core.connectivity.NetworkStatus
import com.example.m3showcase.core.designsystem.component.ConnectivityBannerState
import com.example.m3showcase.core.domain.model.EventTheme
import com.example.m3showcase.core.domain.model.ThemeMode
import com.example.m3showcase.core.domain.usecase.ResolveEventThemeUseCase
import com.example.m3showcase.core.domain.usecase.ObserveNetworkStatusUseCase
import com.example.m3showcase.core.domain.usecase.ObserveSessionUseCase
import com.example.m3showcase.core.domain.usecase.ObserveSettingsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest

sealed interface MainUiState {
    /** Settings not read yet: keep the splash up so the theme doesn't flash. */
    data object Loading : MainUiState
    data class Ready(
        val themeMode: ThemeMode,
        /** Already resolved: Auto has been turned into today's festival or Default. */
        val eventTheme: EventTheme,
        val isLoggedIn: Boolean,
    ) : MainUiState
}

/** App-level state: theme, session and the connectivity banner. */
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(
    observeSettings: ObserveSettingsUseCase,
    observeSession: ObserveSessionUseCase,
    observeNetworkStatus: ObserveNetworkStatusUseCase,
    resolveEventTheme: ResolveEventThemeUseCase,
) : ViewModel() {

    // Auto is resolved when settings emit, i.e. on app start and on every theme change.
    // That's enough for festivals measured in days; no midnight timer needed.
    val uiState: StateFlow<MainUiState> = combine(
        observeSettings(),
        observeSession().map { it != null },
    ) { settings, loggedIn ->
        MainUiState.Ready(settings.themeMode, resolveEventTheme(settings.eventTheme), loggedIn)
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState.Loading)

    /**
     * Turns the online/offline stream into banner states:
     *   offline            → Offline (stays until back online)
     *   offline → online   → BackOnline for 2s, then Hidden
     *   online at start    → Hidden
     * runningFold keeps the previous status. transformLatest cancels the 2s timer
     * if the network drops again before it finishes.
     */
    val connectivityBanner: StateFlow<ConnectivityBannerState> = observeNetworkStatus()
        .runningFold(null as Pair<NetworkStatus?, NetworkStatus>?) { previous, current -> previous?.second to current }
        .transformLatest { pair ->
            val (previous, current) = pair ?: return@transformLatest
            when {
                current == NetworkStatus.Unavailable -> emit(ConnectivityBannerState.Offline)
                previous == NetworkStatus.Unavailable -> {
                    emit(ConnectivityBannerState.BackOnline)
                    delay(2_000)
                    emit(ConnectivityBannerState.Hidden)
                }
                else -> emit(ConnectivityBannerState.Hidden)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConnectivityBannerState.Hidden)
}
