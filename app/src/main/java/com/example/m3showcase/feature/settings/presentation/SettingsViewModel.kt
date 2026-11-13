package com.example.m3showcase.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m3showcase.core.domain.usecase.LogoutUseCase
import com.example.m3showcase.core.domain.usecase.ObserveSessionUseCase
import com.example.m3showcase.core.domain.usecase.ObserveSettingsUseCase
import com.example.m3showcase.core.domain.usecase.SetBalanceHiddenUseCase
import com.example.m3showcase.core.domain.usecase.SetSessionTimeoutUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.m3showcase.core.domain.usecase.ObserveAppLanguageUseCase
import com.example.m3showcase.core.language.LanguageSwitchController

class SettingsViewModel(
    observeSettings: ObserveSettingsUseCase,
    observeSession: ObserveSessionUseCase,
    private val setBalanceHidden: SetBalanceHiddenUseCase,
    private val logout: LogoutUseCase,
    private val setSessionTimeout: SetSessionTimeoutUseCase,
    observeAppLanguage: ObserveAppLanguageUseCase,
    private val languageSwitch: LanguageSwitchController,
) : ViewModel() {

    private val showLogoutDialog = MutableStateFlow(false)
    private val showSessionTimeoutDialog = MutableStateFlow(false)
    private val showLanguageDialog = MutableStateFlow(false)

    /** Dialog flags grouped so the main combine stays within five flows. */
    private val dialogs = combine(showLogoutDialog, showSessionTimeoutDialog, showLanguageDialog, ::Triple)

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events: Flow<SettingsEvent> = _events.receiveAsFlow()

    val viewState: StateFlow<SettingsViewState> = combine(
        observeSettings(), // cold (DataStore)
        observeSession(),  // hot (StateFlow)
        observeAppLanguage(), // hot (platform per-app locale)
        dialogs,           // hot (local)
    ) { settings, user, language, (logoutDialog, timeoutDialog, languageDialog) ->
        SettingsViewState.Success(
            user = user,
            themeMode = settings.themeMode,
            eventTheme = settings.eventTheme,
            isBalanceHidden = settings.isBalanceHidden,
            showLogoutDialog = logoutDialog,
            sessionTimeout = settings.sessionTimeout,
            showSessionTimeoutDialog = timeoutDialog,
            language = language,
            showLanguageDialog = languageDialog,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsViewState.Loading)

    fun onAction(action: SettingsViewAction) {
        when (action) {
            SettingsViewAction.OnThemeClicked -> _events.trySend(SettingsEvent.OpenTheme)
            is SettingsViewAction.OnHideBalanceChanged -> viewModelScope.launch { setBalanceHidden(action.hidden) }
            SettingsViewAction.OnOpenShowcaseClicked -> _events.trySend(SettingsEvent.OpenShowcase)
            SettingsViewAction.OnLogoutClicked -> showLogoutDialog.value = true
            SettingsViewAction.OnLogoutDismissed -> showLogoutDialog.value = false
            SettingsViewAction.OnLogoutConfirmed -> viewModelScope.launch {
                showLogoutDialog.value = false
                logout() // The session becomes null; BankApp sends the user back to Login.
            }
            SettingsViewAction.OnSessionTimeoutClicked -> showSessionTimeoutDialog.value = true
            SettingsViewAction.OnSessionTimeoutDismissed -> showSessionTimeoutDialog.value = false
            is SettingsViewAction.OnSessionTimeoutSelected -> viewModelScope.launch {
                showSessionTimeoutDialog.value = false
                setSessionTimeout(action.timeout) // SessionTimeoutMonitor picks it up from DataStore.
            }
            SettingsViewAction.OnLanguageClicked -> showLanguageDialog.value = true
            SettingsViewAction.OnLanguageDismissed -> showLanguageDialog.value = false
            is SettingsViewAction.OnLanguageSelected -> {
                showLanguageDialog.value = false
                // Covers the screen, applies the language (the Activity recreates underneath), then reveals.
                languageSwitch.switchTo(action.language)
            }
        }
    }
}
