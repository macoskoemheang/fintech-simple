package com.example.m3showcase.feature.loans.data

import com.example.m3showcase.core.common.DateProvider
import com.example.m3showcase.core.data.remote.FakeNetwork
import com.example.m3showcase.core.data.remote.dto.ApiException
import com.example.m3showcase.core.domain.model.plusMonths
import com.example.m3showcase.core.domain.model.toIsoString

data class LoanDto(
    val id: String,
    val productName: String,
    val loanNumber: String,
    val type: String,
    val principalMinor: Long,
    val currency: String,
    val rateBp: Int,
    val termMonths: Int,
    val disbursedOn: String,
)

/** Stand-in for `GET /loans` and `GET /loans/{id}`. Dates are relative to today. */
class FakeLoanRemoteDataSource(private val network: FakeNetwork, dateProvider: DateProvider) {

    private val loans: List<LoanDto> = dateProvider.today().let { today ->
        fun monthsAgo(n: Int) = today.plusMonths(-n).toIsoString()
        listOf(
            LoanDto("loan-01", "Personal Loan", "300 118 420", "PERSONAL", 800_000, "USD", 950, 36, monthsAgo(14)),
            LoanDto("loan-02", "Car Loan", "300 204 917", "CAR", 1_800_000, "USD", 725, 60, monthsAgo(26)),
            LoanDto("loan-03", "SME Business Loan", "300 377 105", "BUSINESS", 40_000_000, "KHR", 1100, 24, monthsAgo(5)),
        )
    }

    suspend fun getLoans(): List<LoanDto> = network.call { loans }

    suspend fun getLoan(id: String): LoanDto = network.call(latencyMs = 500) {
        loans.firstOrNull { it.id == id } ?: throw ApiException(404, "NOT_FOUND")
    }
}
