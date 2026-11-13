package com.example.m3showcase.feature.deposits.data

import com.example.m3showcase.core.common.DateProvider
import com.example.m3showcase.core.data.remote.FakeNetwork
import com.example.m3showcase.core.data.remote.dto.ApiException
import com.example.m3showcase.core.domain.model.plusMonths
import com.example.m3showcase.core.domain.model.toIsoString

data class DepositDto(
    val id: String,
    val productName: String,
    val accountNumber: String,
    val principalMinor: Long,
    val currency: String,
    val rateBp: Int,
    val termMonths: Int,
    val startDate: String,
    val payout: String,
    val autoRenew: Boolean,
)

/** Stand-in for `GET /deposits` and `GET /deposits/{id}`. Start dates are relative to today. */
class FakeDepositRemoteDataSource(private val network: FakeNetwork, dateProvider: DateProvider) {

    private val deposits: List<DepositDto> = dateProvider.today().let { today ->
        fun startedMonthsAgo(n: Int) = today.plusMonths(-n).toIsoString()
        listOf(
            DepositDto("dep-01", "Fixed Deposit 12M", "100 245 118", 500_000, "USD", 525, 12, startedMonthsAgo(7), "MONTHLY", true),
            DepositDto("dep-02", "Fixed Deposit 6M", "100 245 207", 200_000, "USD", 450, 6, startedMonthsAgo(2), "MATURITY", false),
            DepositDto("dep-03", "Riel Saver 24M", "100 311 054", 20_000_000, "KHR", 675, 24, startedMonthsAgo(10), "MONTHLY", true),
            DepositDto("dep-04", "Fixed Deposit 3M", "100 198 660", 150_000, "USD", 400, 3, startedMonthsAgo(5), "MATURITY", false),
        )
    }

    suspend fun getDeposits(): List<DepositDto> = network.call { deposits }

    suspend fun getDeposit(id: String): DepositDto = network.call(latencyMs = 500) {
        deposits.firstOrNull { it.id == id } ?: throw ApiException(404, "NOT_FOUND")
    }
}
