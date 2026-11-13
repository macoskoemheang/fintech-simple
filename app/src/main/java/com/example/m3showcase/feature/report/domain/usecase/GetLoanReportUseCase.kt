package com.example.m3showcase.feature.report.domain.usecase

import com.example.m3showcase.feature.report.domain.model.LoanReport
import com.example.m3showcase.feature.report.domain.model.LoanReportSummary
import com.example.m3showcase.feature.report.domain.model.TriggerReport
import com.example.m3showcase.feature.report.domain.repository.LoanReportRepository

class GetLoanReportUseCase(private val repository: LoanReportRepository) {

    suspend operator fun invoke(days: Int = DEFAULT_DAYS): Result<LoanReport> =
        repository.getTriggerReport(days).map { LoanReport(it, summarize(it)) }

    companion object {
        const val DEFAULT_DAYS = 30

        fun summarize(report: TriggerReport): LoanReportSummary {
            val daily = List(report.dayCount) { day -> report.rows.sumOf { it.dailyCounts.getOrElse(day) { 0 } } }
            val total = daily.sum()
            return LoanReportSummary(
                totalAlerts = total,
                dailyTotals = daily,
                busiestDayIndex = if (total == 0) null else daily.indices.maxBy { daily[it] },
                topTrigger = report.rows.filter { it.total > 0 }.maxByOrNull { it.total },
                activeTriggerTypes = report.rows.count { it.total > 0 },
            )
        }
    }
}
