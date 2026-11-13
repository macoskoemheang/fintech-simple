package com.example.m3showcase.core.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.m3showcase.core.domain.model.EventTheme
import com.example.m3showcase.core.domain.model.SessionTimeout
import com.example.m3showcase.core.domain.model.ThemeMode
import com.example.m3showcase.core.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class SettingsLocalDataSource(context: Context) {

    private val dataStore = context.settingsDataStore

    /** COLD: DataStore reads the file only while someone collects. */
    val settings: Flow<UserSettings> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            UserSettings(
                themeMode = prefs[THEME_MODE]
                    ?.let { saved -> ThemeMode.entries.firstOrNull { it.name == saved } }
                    ?: ThemeMode.System,
                isBalanceHidden = prefs[BALANCE_HIDDEN] ?: false,
                // Unknown names (e.g. a theme removed in a later version) fall back to Auto.
                eventTheme = prefs[EVENT_THEME]
                    ?.let { saved -> EventTheme.entries.firstOrNull { it.name == saved } }
                    ?: EventTheme.Auto,
                sessionTimeout = prefs[SESSION_TIMEOUT]
                    ?.let { saved -> SessionTimeout.entries.firstOrNull { it.name == saved } }
                    ?: SessionTimeout.Default,
            )
        }
        .distinctUntilChanged()

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[THEME_MODE] = mode.name }
    }

    suspend fun setBalanceHidden(hidden: Boolean) {
        dataStore.edit { it[BALANCE_HIDDEN] = hidden }
    }

    suspend fun setEventTheme(theme: EventTheme) {
        dataStore.edit { it[EVENT_THEME] = theme.name }
    }

    suspend fun setSessionTimeout(timeout: SessionTimeout) {
        dataStore.edit { it[SESSION_TIMEOUT] = timeout.name }
    }

    private companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val BALANCE_HIDDEN = booleanPreferencesKey("balance_hidden")
        val EVENT_THEME = stringPreferencesKey("event_theme")
        val SESSION_TIMEOUT = stringPreferencesKey("session_timeout")
    }
}
