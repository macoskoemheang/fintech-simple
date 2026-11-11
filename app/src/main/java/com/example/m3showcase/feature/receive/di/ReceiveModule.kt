package com.example.m3showcase.feature.receive.di

import com.example.m3showcase.feature.receive.domain.usecase.ObserveReceiveQrUseCase
import com.example.m3showcase.feature.receive.presentation.ReceiveViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val receiveModule = module {
    factoryOf(::ObserveReceiveQrUseCase)
    viewModelOf(::ReceiveViewModel)
}
