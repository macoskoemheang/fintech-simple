package com.example.m3showcase.core.data.local

import com.example.m3showcase.core.domain.model.Account
import com.example.m3showcase.core.domain.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory, HOT cache: the single source of truth that screens observe.
 * `null` means "never loaded", which is different from "loaded and empty".
 *
 * Swap this for Room DAOs (which also return Flows) to keep data across restarts.
 */
class BankLocalDataSource {

    private val _accounts = MutableStateFlow<List<Account>?>(null)
    val accounts: StateFlow<List<Account>?> = _accounts.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>?>(null)
    val transactions: StateFlow<List<Transaction>?> = _transactions.asStateFlow()

    fun saveAccounts(accounts: List<Account>) {
        _accounts.value = accounts
    }

    fun upsertAccounts(changed: List<Account>) = _accounts.update { current ->
        val byId = changed.associateBy { it.id }
        current?.map { byId[it.id] ?: it }
    }

    fun saveTransactions(transactions: List<Transaction>) {
        _transactions.value = transactions.sortedByDescending { it.timestampMillis }
    }

    fun addTransactions(new: List<Transaction>) = _transactions.update { current ->
        (new + current.orEmpty()).sortedByDescending { it.timestampMillis }
    }

    fun clear() {
        _accounts.value = null
        _transactions.value = null
    }
}
