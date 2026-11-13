package com.example.m3showcase.feature.settings.presentation

import androidx.compose.runtime.Immutable
import com.example.m3showcase.core.domain.model.EventTheme
import com.example.m3showcase.core.domain.model.SessionTimeout
import com.example.m3showcase.core.domain.model.ThemeMode
import com.example.m3showcase.core.domain.model.UserProfile

@Immutable
sealed interface SettingsViewState {
    data object Loading : SettingsViewState
    data class Success(
        val user: UserProfile?,
        val themeMode: ThemeMode,
        val eventTheme: EventTheme,
        val isBalanceHidden: Boolean,
        val showLogoutDialog: Boolean,
        val sessionTimeout: SessionTimeout,
        val showSessionTimeoutDialog: Boolean,
    ) : SettingsViewState
}

sealed interface SettingsViewAction {
    data object OnThemeClicked : SettingsViewAction
    data class OnHideBalanceChanged(val hidden: Boolean) : SettingsViewAction
    data object OnOpenShowcaseClicked : SettingsViewAction
    data object OnLogoutClicked : SettingsViewAction
    data object OnLogoutConfirmed : SettingsViewAction
    data object OnLogoutDismissed : SettingsViewAction
    data object OnSessionTimeoutClicked : SettingsViewAction
    data class OnSessionTimeoutSelected(val timeout: SessionTimeout) : SettingsViewAction
    data object OnSessionTimeoutDismissed : SettingsViewAction
}

sealed interface SettingsEvent {
    data object OpenTheme : SettingsEvent
    data object OpenShowcase : SettingsEvent
}
