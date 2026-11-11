package com.example.m3showcase.feature.accounts.domain.usecase

import com.example.m3showcase.core.domain.repository.TransactionRepository
import com.example.m3showcase.core.domain.usecase.ObserveAccountsUseCase
import com.example.m3showcase.core.domain.usecase.ObserveSettingsUseCase
import com.example.m3showcase.feature.accounts.domain.model.TransactionFilter
import com.example.m3showcase.feature.accounts.domain.model.TransactionHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveTransactionHistoryUseCase(
    private val observeAccounts: ObserveAccountsUseCase,
    private val transactionRepository: TransactionRepository,
    private val observeSettings: ObserveSettingsUseCase,
) {
    operator fun invoke(accountId: String?, filter: TransactionFilter): Flow<TransactionHistory> = combine(
        observeAccounts(),
        transactionRepository.observeTransactions(accountId),
        observeSettings(),
    ) { accounts, transactions, settings ->
        TransactionHistory(
            account = accountId?.let { id -> accounts.firstOrNull { it.id == id } },
            transactions = when (filter) {
                TransactionFilter.All -> transactions
                TransactionFilter.MoneyIn -> transactions.filter { it.isCredit }
                TransactionFilter.MoneyOut -> transactions.filterNot { it.isCredit }
            },
            isBalanceHidden = settings.isBalanceHidden,
        )
    }
}
