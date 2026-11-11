package com.example.m3showcase.feature.receipt.di

import com.example.m3showcase.feature.receipt.presentation.ReceiptViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val receiptModule = module {
    viewModel { params ->
        ReceiptViewModel(
            transactionId = params.get(0),
            isNewTransfer = params.get(1),
            getTransaction = get(),
        )
    }
}
