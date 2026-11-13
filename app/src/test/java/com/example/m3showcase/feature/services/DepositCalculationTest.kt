package com.example.m3showcase.feature.services

import com.example.m3showcase.core.domain.model.CalendarDate
import com.example.m3showcase.core.domain.model.CurrencyCode
import com.example.m3showcase.core.domain.model.Money
import com.example.m3showcase.feature.deposits.domain.model.DepositStatus
import com.example.m3showcase.feature.deposits.domain.model.InterestPayout
import com.example.m3showcase.feature.deposits.domain.model.TermDeposit
import com.example.m3showcase.feature.deposits.domain.usecase.CalculateDepositUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class DepositCalculationTest {
    private fun deposit(payout: InterestPayout) = TermDeposit(
        "d", "FD", "100", Money(500_000, CurrencyCode.USD), annualRateBp = 525, termMonths = 12,
        startDate = CalendarDate(2026, 1, 10), payout = payout, autoRenew = false,
    )

    private fun calc(today: CalendarDate) = CalculateDepositUseCase { today }

    @Test fun `monthly interest is principal times rate over twelve, rounded half up`() {
        // $5,000 × 5.25% ÷ 12 = $21.875 → $21.88
        assertEquals(2_188L, CalculateDepositUseCase.monthlyInterestMinor(500_000, 525))
    }

    @Test fun `monthly payout pays every month and counts what has passed`() {
        val d = calc(CalendarDate(2026, 8, 20))(deposit(InterestPayout.Monthly))
        assertEquals(CalendarDate(2027, 1, 10), d.maturityDate)
        assertEquals(12, d.schedule.size)
        assertEquals(7, d.schedule.count { it.isPaid })
        assertEquals(Money(7 * 2_188, CurrencyCode.USD), d.interestToDate)
        assertEquals(Money(500_000 + 12 * 2_188, CurrencyCode.USD), d.maturityAmount)
        assertEquals(DepositStatus.Active, d.status)
    }

    @Test fun `at-maturity payout is one payment, and the deposit matures on the date`() {
        val d = calc(CalendarDate(2027, 1, 10))(deposit(InterestPayout.AtMaturity))
        assertEquals(1, d.schedule.size)
        assertEquals(Money(12 * 2_188, CurrencyCode.USD), d.schedule.single().amount)
        assertEquals(DepositStatus.Matured, d.status)
        assertEquals(1f, d.progress)
        assertEquals(0L, d.daysToMaturity)
    }
}
