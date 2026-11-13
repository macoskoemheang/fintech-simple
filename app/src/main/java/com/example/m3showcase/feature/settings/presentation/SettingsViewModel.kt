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

class SettingsViewModel(
    observeSettings: ObserveSettingsUseCase,
    observeSession: ObserveSessionUseCase,
    private val setBalanceHidden: SetBalanceHiddenUseCase,
    private val logout: LogoutUseCase,
    private val setSessionTimeout: SetSessionTimeoutUseCase,
) : ViewModel() {

    private val showLogoutDialog = MutableStateFlow(false)
    private val showSessionTimeoutDialog = MutableStateFlow(false)

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events: Flow<SettingsEvent> = _events.receiveAsFlow()

    val viewState: StateFlow<SettingsViewState> = combine(
        observeSettings(), // cold (DataStore)
        observeSession(),  // hot (StateFlow)
        showLogoutDialog,  // hot (local)
        showSessionTimeoutDialog, // hot (local)
    ) { settings, user, logoutDialog, timeoutDialog ->
        SettingsViewState.Success(
            user = user,
            themeMode = settings.themeMode,
            eventTheme = settings.eventTheme,
            isBalanceHidden = settings.isBalanceHidden,
            showLogoutDialog = logoutDialog,
            sessionTimeout = settings.sessionTimeout,
            showSessionTimeoutDialog = timeoutDialog,
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
        }
    }
}
