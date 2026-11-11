package com.example.m3showcase.feature.home.domain.usecase

import com.example.m3showcase.core.domain.repository.TransactionRepository
import com.example.m3showcase.core.domain.usecase.ObserveAccountsUseCase
import com.example.m3showcase.core.domain.usecase.ObserveSessionUseCase
import com.example.m3showcase.core.domain.usecase.ObserveSettingsUseCase
import com.example.m3showcase.feature.home.domain.model.HomeDashboard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Combines four streams into one dashboard. When any of them emits (a transfer
 * changes the cache, the user hides balances, …) the dashboard is rebuilt.
 */
class ObserveHomeDashboardUseCase(
    private val observeAccounts: ObserveAccountsUseCase,
    private val transactionRepository: TransactionRepository,
    private val observeSession: ObserveSessionUseCase,
    private val observeSettings: ObserveSettingsUseCase,
) {
    operator fun invoke(): Flow<HomeDashboard> = combine(
        observeAccounts(),
        transactionRepository.observeTransactions().map { it.take(RECENT_COUNT) },
        observeSession(),
        observeSettings().map { it.isBalanceHidden }.distinctUntilChanged(),
    ) { accounts, recent, user, hidden ->
        HomeDashboard(user = user, accounts = accounts, recentTransactions = recent, isBalanceHidden = hidden)
    }

    private companion object {
        const val RECENT_COUNT = 5
    }
}
