package com.example.m3showcase.feature.services

import com.example.m3showcase.core.domain.model.CalendarDate
import com.example.m3showcase.core.domain.model.CurrencyCode
import com.example.m3showcase.core.domain.model.Money
import com.example.m3showcase.feature.loans.domain.model.InstallmentStatus
import com.example.m3showcase.feature.loans.domain.model.Loan
import com.example.m3showcase.feature.loans.domain.model.LoanType
import com.example.m3showcase.feature.loans.domain.usecase.CalculateLoanUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoanScheduleTest {
    private val loan = Loan("l", "Personal", "300", LoanType.Personal, Money(800_000, CurrencyCode.USD), 950, 36, CalendarDate(2025, 1, 5))

    @Test fun `annuity payment matches the standard formula`() {
        // $8,000 at 9.5% over 36 months ≈ $256.26
        assertEquals(25_626L, CalculateLoanUseCase.monthlyPaymentMinor(800_000, 950, 36))
        assertEquals(10_000L, CalculateLoanUseCase.monthlyPaymentMinor(120_000, 0, 12))
    }

    @Test fun `principal parts repay exactly the amount borrowed`() {
        val d = CalculateLoanUseCase { CalendarDate(2025, 1, 5) }(loan)
        assertEquals(800_000L, d.schedule.sumOf { it.principalPart.minor })
        assertEquals(0L, d.schedule.last().balanceAfter.minor)
        d.schedule.forEach { assertEquals(it.payment.minor, it.principalPart.minor + it.interestPart.minor) }
    }

    @Test fun `past installments are paid and the next one is due`() {
        val d = CalculateLoanUseCase { CalendarDate(2026, 3, 1) }(loan)
        assertEquals(13, d.paidCount) // Feb 2025 … Feb 2026
        assertEquals(CalendarDate(2026, 3, 5), d.nextInstallment?.dueDate)
        assertEquals(1, d.schedule.count { it.status == InstallmentStatus.Due })
        assertEquals(d.schedule[12].balanceAfter, d.outstanding)
        assertTrue(d.progress in 0.3f..0.4f)
    }

    @Test fun `a loan past its last date is paid off`() {
        val d = CalculateLoanUseCase { CalendarDate(2030, 1, 1) }(loan)
        assertNull(d.nextInstallment)
        assertEquals(0L, d.outstanding.minor)
        assertEquals(1f, d.progress)
    }
}
