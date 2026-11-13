package com.example.m3showcase.feature.payments.data

import com.example.m3showcase.core.data.remote.safeApiCall
import com.example.m3showcase.core.domain.model.CurrencyCode
import com.example.m3showcase.core.domain.model.Money
import com.example.m3showcase.core.domain.model.Page
import com.example.m3showcase.core.domain.model.PageRequest
import com.example.m3showcase.feature.payments.domain.model.BillerCategory
import com.example.m3showcase.feature.payments.domain.model.Payment
import com.example.m3showcase.feature.payments.domain.model.PaymentStatus
import com.example.m3showcase.feature.payments.domain.model.PaymentSummary
import com.example.m3showcase.feature.payments.domain.repository.PaymentRepository

class PaymentRepositoryImpl(private val remote: FakePaymentRemoteDataSource) : PaymentRepository {

    override suspend fun getPayments(category: BillerCategory?, request: PageRequest): Result<Page<Payment>> = safeApiCall {
        val dto = remote.getPayments(request.cursor?.toLongOrNull(), request.size, category?.name?.uppercase())
        Page(dto.data.map { it.toDomain() }, dto.nextBefore?.toString(), dto.total)
    }

    override suspend fun getPayment(id: String): Result<Payment> = safeApiCall { remote.getPayment(id).toDomain() }

    override suspend fun getSummary(): Result<PaymentSummary> = safeApiCall {
        val dto = remote.getSummary()
        PaymentSummary(Money(dto.paidThisMonthMinor, CurrencyCode.valueOf(dto.currency)), dto.count, dto.pending)
    }
}

private fun PaymentDto.toDomain(): Payment {
    val currency = CurrencyCode.valueOf(currency)
    return Payment(
        id = id,
        biller = billerName,
        category = BillerCategory.entries.firstOrNull { it.name.equals(category, ignoreCase = true) } ?: BillerCategory.Mobile,
        customerRef = customerRef,
        amount = Money(amountMinor, currency),
        fee = Money(feeMinor, currency),
        status = when (status) { "PENDING" -> PaymentStatus.Pending; "FAILED" -> PaymentStatus.Failed; else -> PaymentStatus.Success },
        timestamp = paidAt,
        fromAccountName = fromAccountName,
        fromAccountNumber = fromAccountNumber,
        reference = reference,
    )
}
