package com.example.m3showcase.feature.report.data

import com.example.m3showcase.core.common.DateProvider
import com.example.m3showcase.core.data.remote.FakeNetwork
import com.example.m3showcase.core.domain.model.plusDays
import com.example.m3showcase.core.domain.model.toIsoString
import kotlin.random.Random

data class TriggerRowDto(val no: Int, val triggerType: String, val dailyCounts: List<Int>)

data class TriggerReportDto(val from: String, val rows: List<TriggerRowDto>)

/** Stand-in for the credit bureau's `GET /triggers?days=` feed. Seeded, so it's the same every run. */
class FakeLoanReportRemoteDataSource(private val network: FakeNetwork, private val dateProvider: DateProvider) {

    suspend fun getTriggers(days: Int): TriggerReportDto = network.call(latencyMs = 900) {
        val random = Random(42)
        TriggerReportDto(
            from = dateProvider.today().plusDays(-(days - 1L)).toIsoString(),
            rows = TriggerTypes.mapIndexed { i, type ->
                TriggerRowDto(i + 1, type, List(days) { if (random.nextFloat() < 0.4f) random.nextInt(1, 4) else 0 })
            },
        )
    }

    private companion object {
        val TriggerTypes = listOf(
            "New Enquiry", "New Account", "Close Account", "Positive Cycle Change",
            "New Write-off", "Update Write-off", "New Address", "Loss Account",
            "Past Due Balance", "Past Due Change", "Past Due Settlement", "Negative Cycle Change",
            "New Identity", "Restructure Loan", "ID Suppression", "3-times Enquiry Alert",
            "Loan Maturity Alert", "Dishonor Check",
        )
    }
}
