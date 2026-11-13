package com.example.m3showcase.feature.receive.di

import com.example.m3showcase.feature.receive.domain.usecase.ObserveReceiveQrUseCase
import com.example.m3showcase.feature.receive.presentation.ReceiveViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import com.example.m3showcase.feature.receive.domain.usecase.BuildReceiveRequestUseCase
import com.example.m3showcase.feature.receive.presentation.detail.ReceiveQrDetailViewModel
import org.koin.core.module.dsl.viewModel

val receiveModule = module {
    factoryOf(::ObserveReceiveQrUseCase)
    viewModelOf(::ReceiveViewModel)
    factoryOf(::BuildReceiveRequestUseCase)
    viewModel { params -> ReceiveQrDetailViewModel(accountNumber = params.get(), observeReceiveQr = get(), buildRequest = get()) }
}
