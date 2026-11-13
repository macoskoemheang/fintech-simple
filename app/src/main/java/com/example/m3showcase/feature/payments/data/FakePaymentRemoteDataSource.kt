package com.example.m3showcase.feature.payments.data

import com.example.m3showcase.core.data.remote.FakeNetwork
import com.example.m3showcase.core.data.remote.dto.ApiException
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class PaymentDto(
    val id: String,
    val billerName: String,
    val category: String,
    val customerRef: String,
    val amountMinor: Long,
    val feeMinor: Long,
    val currency: String,
    val status: String,
    val paidAt: Long,
    val fromAccountName: String,
    val fromAccountNumber: String,
    val reference: String,
)

data class PaymentPageDto(val data: List<PaymentDto>, val nextBefore: Long?, val total: Int)

data class PaymentSummaryDto(val paidThisMonthMinor: Long, val currency: String, val count: Int, val pending: Int)

/** Stand-in for `GET /payments?before=&limit=&category=`, `GET /payments/{id}`, `GET /payments/summary`. */
class FakePaymentRemoteDataSource(private val network: FakeNetwork) {

    private val payments: List<PaymentDto> = seed(System.currentTimeMillis())

    suspend fun getPayments(before: Long?, limit: Int, category: String?): PaymentPageDto = network.call {
        val matching = payments.filter { category == null || it.category == category }
        val older = matching.filter { before == null || it.paidAt < before }
        val page = older.take(limit)
        PaymentPageDto(page, page.lastOrNull()?.paidAt?.takeIf { older.size > page.size }, matching.size)
    }

    suspend fun getPayment(id: String): PaymentDto = network.call(latencyMs = 500) {
        payments.firstOrNull { it.id == id } ?: throw ApiException(404, "NOT_FOUND")
    }

    suspend fun getSummary(): PaymentSummaryDto = network.call(latencyMs = 400) {
        val monthStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
        }.timeInMillis
        val thisMonth = payments.filter { it.paidAt >= monthStart && it.currency == "USD" }
        PaymentSummaryDto(
            paidThisMonthMinor = thisMonth.filter { it.status == "SUCCESS" }.sumOf { it.amountMinor + it.feeMinor },
            currency = "USD",
            count = thisMonth.size,
            pending = payments.count { it.status == "PENDING" },
        )
    }

    companion object {
        private data class Biller(val name: String, val category: String, val ref: String, val baseMinor: Long)

        private val Billers = listOf(
            Biller("Electricité du Cambodge (EDC)", "ELECTRICITY", "EDC-0045-7781", 4_250),
            Biller("Phnom Penh Water Supply", "WATER", "PPWSA-118204", 1_180),
            Biller("Ezecom Fiber", "INTERNET", "EZ-552019", 2_500),
            Biller("Smart Axiata", "MOBILE", "010 345 678", 500),
            Biller("Royal University of Phnom Penh", "EDUCATION", "RUPP-2024-0915", 32_000),
            Biller("Forte Insurance", "INSURANCE", "FI-PL-778120", 6_800),
            Biller("Cellcard", "MOBILE", "012 345 678", 1_000),
            Biller("Opennet", "INTERNET", "ON-90112", 3_000),
        )

        /** Deterministic history: a payment every ~1.7 days, a couple pending and failed. */
        internal fun seed(now: Long): List<PaymentDto> = List(42) { i ->
            val biller = Billers[(i * 5 + i / 3) % Billers.size]
            val variation = (i * 37 % 23) * 25L
            PaymentDto(
                id = "pay-%03d".format(i),
                billerName = biller.name,
                category = biller.category,
                customerRef = biller.ref,
                amountMinor = biller.baseMinor + variation,
                feeMinor = if (biller.category == "EDUCATION") 100 else 0,
                currency = "USD",
                status = when (i) { 1, 9 -> "PENDING"; 6, 23 -> "FAILED"; else -> "SUCCESS" },
                paidAt = now - TimeUnit.MINUTES.toMillis(40L + i * 2_460L),
                fromAccountName = if (i % 4 == 3) "Business USD" else "Savings USD",
                fromAccountNumber = if (i % 4 == 3) "000789012" else "000123456",
                reference = "BP%08d".format(48_211_000 + i * 7),
            )
        }
    }
}
