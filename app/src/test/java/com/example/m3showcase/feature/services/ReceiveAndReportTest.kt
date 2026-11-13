package com.example.m3showcase.feature.services

import com.example.m3showcase.core.domain.model.BankError
import com.example.m3showcase.core.domain.model.CalendarDate
import com.example.m3showcase.core.domain.model.CurrencyCode
import com.example.m3showcase.core.domain.model.Money
import com.example.m3showcase.core.domain.model.ReceiveQr
import com.example.m3showcase.feature.receive.domain.usecase.BuildReceiveRequestUseCase
import com.example.m3showcase.feature.report.domain.model.TriggerReport
import com.example.m3showcase.feature.report.domain.model.TriggerRow
import com.example.m3showcase.feature.report.domain.usecase.GetLoanReportUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReceiveAndReportTest {
    private val usd = ReceiveQr("SOK DARA", "000123456", CurrencyCode.USD, "novabank://pay?acc=000123456")
    private val khr = usd.copy(currency = CurrencyCode.KHR)
    private val build = BuildReceiveRequestUseCase()

    @Test fun `empty amount keeps the static QR`() {
        val r = build(usd, "", "  ").getOrThrow()
        assertNull(r.amount)
        assertNull(r.note)
        assertEquals(usd.payload, r.payload)
    }

    @Test fun `amount and note go into the payload`() {
        val r = build(usd, "25.5", "Lunch & coffee").getOrThrow()
        assertEquals(Money(2_550, CurrencyCode.USD), r.amount)
        assertEquals("${usd.payload}&amt=25.50&note=Lunch+%26+coffee", r.payload)
    }

    @Test fun `invalid amounts are rejected with the rule`() {
        fun rule(qr: ReceiveQr, text: String) = (build(qr, text, "").exceptionOrNull() as BankError.Validation).rule
        assertEquals(BankError.Rule.NotPositive, rule(usd, "0"))
        assertEquals(BankError.Rule.TooManyDecimals, rule(usd, "1.234"))
        assertEquals(BankError.Rule.TooManyDecimals, rule(khr, "100.5"))
        assertEquals(BankError.Rule.AboveLimit, rule(usd, "10000.01"))
        assertEquals(BankError.Rule.NotANumber, rule(usd, "."))
    }

    @Test fun `report summary finds totals, busiest day and top trigger`() {
        val report = TriggerReport(
            CalendarDate(2026, 11, 1),
            listOf(TriggerRow(1, "New Enquiry", listOf(1, 0, 3)), TriggerRow(2, "Close Account", listOf(0, 0, 1)), TriggerRow(3, "Dishonor Check", listOf(0, 0, 0))),
        )
        val s = GetLoanReportUseCase.summarize(report)
        assertEquals(5, s.totalAlerts)
        assertEquals(listOf(1, 0, 4), s.dailyTotals)
        assertEquals(2, s.busiestDayIndex)
        assertEquals("New Enquiry", s.topTrigger?.triggerType)
        assertEquals(2, s.activeTriggerTypes)
    }

    @Test fun `empty report has no busiest day`() {
        val s = GetLoanReportUseCase.summarize(TriggerReport(CalendarDate(2026, 11, 1), listOf(TriggerRow(1, "X", listOf(0, 0)))))
        assertNull(s.busiestDayIndex)
        assertNull(s.topTrigger)
        assertTrue(s.dailyTotals.all { it == 0 })
    }
}
