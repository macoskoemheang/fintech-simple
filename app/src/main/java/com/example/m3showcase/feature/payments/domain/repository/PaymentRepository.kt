package com.example.m3showcase.feature.payments.domain.repository

import com.example.m3showcase.core.domain.model.Page
import com.example.m3showcase.core.domain.model.PageRequest
import com.example.m3showcase.feature.payments.domain.model.BillerCategory
import com.example.m3showcase.feature.payments.domain.model.Payment
import com.example.m3showcase.feature.payments.domain.model.PaymentSummary

interface PaymentRepository {
    /** Newest first. [category] null means every category. */
    suspend fun getPayments(category: BillerCategory?, request: PageRequest): Result<Page<Payment>>
    suspend fun getPayment(id: String): Result<Payment>
    suspend fun getSummary(): Result<PaymentSummary>
}
