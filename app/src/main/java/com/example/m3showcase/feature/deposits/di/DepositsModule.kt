package com.example.m3showcase.feature.deposits.di

import com.example.m3showcase.feature.deposits.data.DepositRepositoryImpl
import com.example.m3showcase.feature.deposits.data.FakeDepositRemoteDataSource
import com.example.m3showcase.feature.deposits.domain.repository.DepositRepository
import com.example.m3showcase.feature.deposits.domain.usecase.CalculateDepositUseCase
import com.example.m3showcase.feature.deposits.domain.usecase.GetDepositDetailUseCase
import com.example.m3showcase.feature.deposits.domain.usecase.GetDepositsUseCase
import com.example.m3showcase.feature.deposits.presentation.detail.DepositDetailViewModel
import com.example.m3showcase.feature.deposits.presentation.list.DepositsViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val depositsModule = module {
    singleOf(::FakeDepositRemoteDataSource)
    singleOf(::DepositRepositoryImpl) { bind<DepositRepository>() }
    factoryOf(::CalculateDepositUseCase)
    factoryOf(::GetDepositsUseCase)
    factoryOf(::GetDepositDetailUseCase)
    viewModelOf(::DepositsViewModel)
    viewModel { params -> DepositDetailViewModel(depositId = params.get(), getDeposit = get()) }
}
