package com.example.m3showcase.feature.report.domain.model

import androidx.compose.runtime.Immutable
import com.example.m3showcase.core.domain.model.CalendarDate

/** One credit-bureau trigger type with its count for each day of the period. */
@Immutable
data class TriggerRow(
    val no: Int,
    /** Bureau's own name for the trigger; shown as received. */
    val triggerType: String,
    /** Count per day; index 0 = first day of the period. */
    val dailyCounts: List<Int>,
) {
    val total: Int get() = dailyCounts.sum()
}

/** Loan report: credit-bureau alerts over a period of days. */
data class TriggerReport(val periodStart: CalendarDate, val rows: List<TriggerRow>) {
    val dayCount: Int get() = rows.firstOrNull()?.dailyCounts?.size ?: 0
}

/** Headline numbers, computed by GetLoanReportUseCase. */
data class LoanReportSummary(
    val totalAlerts: Int,
    /** Alerts per day, summed over every trigger type. */
    val dailyTotals: List<Int>,
    /** 0-based index into [dailyTotals]; null when there were no alerts. */
    val busiestDayIndex: Int?,
    val topTrigger: TriggerRow?,
    val activeTriggerTypes: Int,
)

data class LoanReport(val report: TriggerReport, val summary: LoanReportSummary)
