package com.example.m3showcase.core.data.repository

import com.example.m3showcase.core.data.local.BankLocalDataSource
import com.example.m3showcase.core.data.local.SettingsLocalDataSource
import com.example.m3showcase.core.data.mapper.toDomain
import com.example.m3showcase.core.data.mapper.toDto
import com.example.m3showcase.core.data.remote.FakeBankRemoteDataSource
import com.example.m3showcase.core.data.remote.safeApiCall
import com.example.m3showcase.core.domain.model.Account
import com.example.m3showcase.core.domain.model.BankError
import com.example.m3showcase.core.domain.model.ThemeMode
import com.example.m3showcase.core.domain.model.Transaction
import com.example.m3showcase.core.domain.model.TransferRequest
import com.example.m3showcase.core.domain.model.UserProfile
import com.example.m3showcase.core.domain.model.UserSettings
import com.example.m3showcase.core.domain.repository.AccountRepository
import com.example.m3showcase.core.domain.repository.AuthRepository
import com.example.m3showcase.core.domain.repository.SettingsRepository
import com.example.m3showcase.core.domain.model.EventTheme
import com.example.m3showcase.core.domain.model.SessionTimeout
import com.example.m3showcase.core.domain.repository.TransactionRepository
import com.example.m3showcase.core.domain.repository.TransferRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val remote: FakeBankRemoteDataSource,
    private val local: BankLocalDataSource,
) : AuthRepository {

    private val _session = MutableStateFlow<UserProfile?>(null)
    override val session: StateFlow<UserProfile?> = _session.asStateFlow()

    override suspend fun login(pin: String): Result<UserProfile> =
        safeApiCall { remote.login(pin).toDomain() }
            .onSuccess { _session.value = it }

    override suspend fun logout() {
        _session.value = null
        local.clear() // Don't leave account data in memory after logout.
    }
}

class AccountRepositoryImpl(
    private val remote: FakeBankRemoteDataSource,
    private val local: BankLocalDataSource,
) : AccountRepository {

    override fun observeAccounts(): Flow<List<Account>> = local.accounts.filterNotNull()

    override suspend fun refreshAccounts(): Result<Unit> =
        safeApiCall { remote.getAccounts() }
            .map { dtos -> local.saveAccounts(dtos.map { it.toDomain() }) }
}

class TransactionRepositoryImpl(
    private val remote: FakeBankRemoteDataSource,
    private val local: BankLocalDataSource,
) : TransactionRepository {

    override fun observeTransactions(accountId: String?): Flow<List<Transaction>> =
        local.transactions.filterNotNull().map { all ->
            if (accountId == null) all else all.filter { it.accountId == accountId }
        }

    override suspend fun refreshTransactions(): Result<Unit> =
        safeApiCall { remote.getTransactions() }
            .map { dtos -> local.saveTransactions(dtos.map { it.toDomain() }) }

    override suspend fun getTransaction(id: String): Result<Transaction> {
        val cached = local.transactions.value?.firstOrNull { it.id == id }
        return if (cached != null) Result.success(cached) else Result.failure(BankError.NotFound)
    }
}

class TransferRepositoryImpl(
    private val remote: FakeBankRemoteDataSource,
    private val local: BankLocalDataSource,
) : TransferRepository {

    override suspend fun transfer(request: TransferRequest): Result<Transaction> =
        safeApiCall { remote.transfer(request.toDto()) }
            .map { response ->
                // Write the server's response into the cache. Every screen observing
                // accounts or transactions (e.g. Home) updates on its own.
                local.upsertAccounts(response.updatedAccounts.map { it.toDomain() })
                local.addTransactions(listOfNotNull(response.debit, response.credit).map { it.toDomain() })
                response.debit.toDomain()
            }
}

class SettingsRepositoryImpl(
    private val local: SettingsLocalDataSource,
) : SettingsRepository {
    override val settings: Flow<UserSettings> = local.settings
    override suspend fun setThemeMode(mode: ThemeMode) = local.setThemeMode(mode)
    override suspend fun setBalanceHidden(hidden: Boolean) = local.setBalanceHidden(hidden)
    override suspend fun setEventTheme(theme: EventTheme) = local.setEventTheme(theme)
    override suspend fun setSessionTimeout(timeout: SessionTimeout) = local.setSessionTimeout(timeout)
}
