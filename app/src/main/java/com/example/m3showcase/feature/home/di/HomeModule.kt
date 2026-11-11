package com.example.m3showcase.feature.home.di

import com.example.m3showcase.feature.home.domain.usecase.ObserveHomeDashboardUseCase
import com.example.m3showcase.feature.home.presentation.HomeViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homeModule = module {
    factoryOf(::ObserveHomeDashboardUseCase)
    viewModelOf(::HomeViewModel)
}
