package com.example.m3showcase.feature.loans.di

import com.example.m3showcase.feature.loans.data.FakeLoanRemoteDataSource
import com.example.m3showcase.feature.loans.data.LoanRepositoryImpl
import com.example.m3showcase.feature.loans.domain.repository.LoanRepository
import com.example.m3showcase.feature.loans.domain.usecase.CalculateLoanUseCase
import com.example.m3showcase.feature.loans.domain.usecase.GetLoanDetailUseCase
import com.example.m3showcase.feature.loans.domain.usecase.GetLoansUseCase
import com.example.m3showcase.feature.loans.presentation.detail.LoanDetailViewModel
import com.example.m3showcase.feature.loans.presentation.list.LoansViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val loansModule = module {
    singleOf(::FakeLoanRemoteDataSource)
    singleOf(::LoanRepositoryImpl) { bind<LoanRepository>() }
    factoryOf(::CalculateLoanUseCase)
    factoryOf(::GetLoansUseCase)
    factoryOf(::GetLoanDetailUseCase)
    viewModelOf(::LoansViewModel)
    viewModel { params -> LoanDetailViewModel(loanId = params.get(), getLoan = get()) }
}
