package com.example.m3showcase.feature.payments.domain.usecase

import com.example.m3showcase.core.domain.model.PageRequest
import com.example.m3showcase.feature.payments.domain.model.BillerCategory
import com.example.m3showcase.feature.payments.domain.repository.PaymentRepository

class GetPaymentsPageUseCase(private val repository: PaymentRepository) {
    suspend operator fun invoke(category: BillerCategory?, request: PageRequest) = repository.getPayments(category, request)
}

class GetPaymentUseCase(private val repository: PaymentRepository) {
    suspend operator fun invoke(id: String) = repository.getPayment(id)
}

class GetPaymentSummaryUseCase(private val repository: PaymentRepository) {
    suspend operator fun invoke() = repository.getSummary()
}
