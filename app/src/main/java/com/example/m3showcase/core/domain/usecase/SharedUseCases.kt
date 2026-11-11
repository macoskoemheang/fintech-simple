package com.example.m3showcase.core.domain.usecase

import com.example.m3showcase.core.connectivity.NetworkMonitor
import com.example.m3showcase.core.connectivity.NetworkStatus
import com.example.m3showcase.core.domain.model.Account
import com.example.m3showcase.core.domain.model.ThemeMode
import com.example.m3showcase.core.domain.model.Transaction
import com.example.m3showcase.core.domain.model.UserProfile
import com.example.m3showcase.core.domain.model.UserSettings
import com.example.m3showcase.core.domain.repository.AccountRepository
import com.example.m3showcase.core.domain.repository.AuthRepository
import com.example.m3showcase.core.domain.repository.SettingsRepository
import com.example.m3showcase.core.domain.repository.TransactionRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/* Use cases shared by several features. Each one does one thing. */

class ObserveAccountsUseCase(private val repository: AccountRepository) {
    /** Default account first, then by name. */
    operator fun invoke(): Flow<List<Account>> = repository.observeAccounts()
        .map { list -> list.sortedWith(compareByDescending<Account> { it.isDefault }.thenBy { it.name }) }
}

/** Refreshes accounts and transactions in parallel. Fails if either call fails. */
class RefreshBankDataUseCase(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(): Result<Unit> = coroutineScope {
        val accounts = async { accountRepository.refreshAccounts() }
        val transactions = async { transactionRepository.refreshTransactions() }
        listOf(accounts.await(), transactions.await())
            .firstOrNull { it.isFailure } ?: Result.success(Unit)
    }
}

class GetTransactionUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(id: String): Result<Transaction> = repository.getTransaction(id)
}

class ObserveSettingsUseCase(private val repository: SettingsRepository) {
    operator fun invoke(): Flow<UserSettings> = repository.settings
}

class SetThemeModeUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(mode: ThemeMode) = repository.setThemeMode(mode)
}

class SetBalanceHiddenUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(hidden: Boolean) = repository.setBalanceHidden(hidden)
}

class ObserveSessionUseCase(private val repository: AuthRepository) {
    operator fun invoke(): StateFlow<UserProfile?> = repository.session
}

class LogoutUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.logout()
}

/** Exposes the app-wide HOT connectivity state. */
class ObserveNetworkStatusUseCase(private val monitor: NetworkMonitor) {
    operator fun invoke(): StateFlow<NetworkStatus> = monitor.status
}
