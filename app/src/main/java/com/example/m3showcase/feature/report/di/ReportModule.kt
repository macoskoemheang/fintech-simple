package com.example.m3showcase.feature.report.di

import com.example.m3showcase.feature.report.data.FakeLoanReportRemoteDataSource
import com.example.m3showcase.feature.report.data.LoanReportRepositoryImpl
import com.example.m3showcase.feature.report.domain.repository.LoanReportRepository
import com.example.m3showcase.feature.report.domain.usecase.GetLoanReportUseCase
import com.example.m3showcase.feature.report.presentation.LoanReportViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val reportModule = module {
    singleOf(::FakeLoanReportRemoteDataSource)
    singleOf(::LoanReportRepositoryImpl) { bind<LoanReportRepository>() }
    factoryOf(::GetLoanReportUseCase)
    viewModelOf(::LoanReportViewModel)
}
