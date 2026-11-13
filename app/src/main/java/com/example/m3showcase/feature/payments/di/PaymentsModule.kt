package com.example.m3showcase.feature.payments.di

import com.example.m3showcase.feature.payments.data.FakePaymentRemoteDataSource
import com.example.m3showcase.feature.payments.data.PaymentRepositoryImpl
import com.example.m3showcase.feature.payments.domain.repository.PaymentRepository
import com.example.m3showcase.feature.payments.domain.usecase.GetPaymentSummaryUseCase
import com.example.m3showcase.feature.payments.domain.usecase.GetPaymentUseCase
import com.example.m3showcase.feature.payments.domain.usecase.GetPaymentsPageUseCase
import com.example.m3showcase.feature.payments.presentation.detail.PaymentDetailViewModel
import com.example.m3showcase.feature.payments.presentation.list.PaymentsViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val paymentsModule = module {
    singleOf(::FakePaymentRemoteDataSource)
    singleOf(::PaymentRepositoryImpl) { bind<PaymentRepository>() }
    factoryOf(::GetPaymentsPageUseCase)
    factoryOf(::GetPaymentUseCase)
    factoryOf(::GetPaymentSummaryUseCase)
    viewModelOf(::PaymentsViewModel)
    viewModel { params -> PaymentDetailViewModel(paymentId = params.get(), getPayment = get()) }
}
