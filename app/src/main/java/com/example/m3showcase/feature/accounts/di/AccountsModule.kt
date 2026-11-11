package com.example.m3showcase.feature.accounts.di

import com.example.m3showcase.feature.accounts.domain.usecase.ObserveTransactionHistoryUseCase
import com.example.m3showcase.feature.accounts.presentation.history.TransactionHistoryViewModel
import com.example.m3showcase.feature.accounts.presentation.list.AccountsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val accountsModule = module {
    factoryOf(::ObserveTransactionHistoryUseCase)
    viewModelOf(::AccountsViewModel)
    // The nullable accountId comes from the nav key via parametersOf(...).
    viewModel { params ->
        TransactionHistoryViewModel(
            accountId = params.getOrNull(),
            observeHistory = get(),
            refreshBankData = get(),
        )
    }
}
