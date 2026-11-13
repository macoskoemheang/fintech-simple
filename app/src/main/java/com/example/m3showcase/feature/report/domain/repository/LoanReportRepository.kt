package com.example.m3showcase.feature.report.domain.repository

import com.example.m3showcase.feature.report.domain.model.TriggerReport

interface LoanReportRepository {
    /** The last [days] days, ending today. */
    suspend fun getTriggerReport(days: Int): Result<TriggerReport>
}
