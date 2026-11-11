package com.example.m3showcase.feature.transfer.di

import com.example.m3showcase.feature.transfer.domain.usecase.MakeTransferUseCase
import com.example.m3showcase.feature.transfer.domain.usecase.ValidateTransferUseCase
import com.example.m3showcase.feature.transfer.presentation.TransferViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val transferModule = module {
    factoryOf(::ValidateTransferUseCase)
    factoryOf(::MakeTransferUseCase)
    viewModel { params ->
        TransferViewModel(
            initialFromAccountId = params.getOrNull(),
            observeAccounts = get(),
            observeNetworkStatus = get(),
            validateTransfer = get(),
            makeTransfer = get(),
        )
    }
}
