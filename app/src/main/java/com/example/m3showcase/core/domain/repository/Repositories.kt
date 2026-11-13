package com.example.m3showcase.core.domain.repository

import com.example.m3showcase.core.domain.model.Account
import com.example.m3showcase.core.domain.model.AppLanguage
import com.example.m3showcase.core.domain.model.EventTheme
import com.example.m3showcase.core.domain.model.SessionTimeout
import com.example.m3showcase.core.domain.model.FestivalWindow
import com.example.m3showcase.core.domain.model.ThemeMode
import com.example.m3showcase.core.domain.model.Transaction
import com.example.m3showcase.core.domain.model.TransferRequest
import com.example.m3showcase.core.domain.model.UserProfile
import com.example.m3showcase.core.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/*
 * Repository contracts. Two styles on purpose:
 *  - `observe…()` returns a Flow backed by a local cache: the UI subscribes once and
 *    re-renders whenever the cache changes (offline-first).
 *  - `suspend` one-shot calls hit the network and return Result.
 */

interface AuthRepository {
    /** Hot: there is exactly one session for the whole app. */
    val session: StateFlow<UserProfile?>
    suspend fun login(pin: String): Result<UserProfile>
    suspend fun logout()
}

interface AccountRepository {
    fun observeAccounts(): Flow<List<Account>>
    suspend fun refreshAccounts(): Result<Unit>
}

interface TransactionRepository {
    fun observeTransactions(accountId: String? = null): Flow<List<Transaction>>
    suspend fun refreshTransactions(): Result<Unit>
    suspend fun getTransaction(id: String): Result<Transaction>
}

interface TransferRepository {
    suspend fun transfer(request: TransferRequest): Result<Transaction>
}

interface SettingsRepository {
    /** Cold Flow straight from DataStore; ViewModels turn it hot with stateIn. */
    val settings: Flow<UserSettings>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setBalanceHidden(hidden: Boolean)
    suspend fun setEventTheme(theme: EventTheme)
    suspend fun setSessionTimeout(timeout: SessionTimeout)
}

/** Festival dates. Lunar holidays move every year, so they come from a schedule, not a rule. */
interface FestivalCalendarRepository {
    /** Windows for [year], sorted by start. Empty when the year isn't known yet. */
    fun windowsFor(year: Int): List<FestivalWindow>
}

/**
 * The app's display language. Stored by the platform (per-app language), not in DataStore,
 * so it also shows in system Settings > Apps > Language on Android 13+ and applies before
 * the first frame. Setting it recreates the Activity in the new language.
 */
interface LanguageRepository {
    val language: StateFlow<AppLanguage>
    fun setLanguage(language: AppLanguage)
}
