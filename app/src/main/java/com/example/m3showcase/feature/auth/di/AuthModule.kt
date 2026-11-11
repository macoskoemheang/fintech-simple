package com.example.m3showcase.feature.auth.di

import com.example.m3showcase.feature.auth.domain.usecase.LoginWithPinUseCase
import com.example.m3showcase.feature.auth.presentation.LoginViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authModule = module {
    factoryOf(::LoginWithPinUseCase)
    viewModelOf(::LoginViewModel)
}
