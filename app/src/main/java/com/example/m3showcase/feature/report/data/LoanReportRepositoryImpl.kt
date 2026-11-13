package com.example.m3showcase.feature.report.data

import com.example.m3showcase.core.data.remote.safeApiCall
import com.example.m3showcase.core.domain.model.parseIsoDate
import com.example.m3showcase.feature.report.domain.model.TriggerReport
import com.example.m3showcase.feature.report.domain.model.TriggerRow
import com.example.m3showcase.feature.report.domain.repository.LoanReportRepository

class LoanReportRepositoryImpl(private val remote: FakeLoanReportRemoteDataSource) : LoanReportRepository {
    override suspend fun getTriggerReport(days: Int): Result<TriggerReport> = safeApiCall {
        val dto = remote.getTriggers(days)
        TriggerReport(parseIsoDate(dto.from), dto.rows.map { TriggerRow(it.no, it.triggerType, it.dailyCounts) })
    }
}
