package com.example.m3showcase.feature.deposits.domain.usecase

import com.example.m3showcase.core.common.DateProvider
import com.example.m3showcase.core.domain.model.CurrencyCode
import com.example.m3showcase.core.domain.model.Money
import com.example.m3showcase.core.domain.model.daysUntil
import com.example.m3showcase.core.domain.model.monthsUntil
import com.example.m3showcase.core.domain.model.plusMonths
import com.example.m3showcase.feature.deposits.domain.model.DepositDetail
import com.example.m3showcase.feature.deposits.domain.model.DepositStatus
import com.example.m3showcase.feature.deposits.domain.model.InterestPayment
import com.example.m3showcase.feature.deposits.domain.model.InterestPayout
import com.example.m3showcase.feature.deposits.domain.model.TermDeposit
import com.example.m3showcase.feature.deposits.domain.repository.DepositRepository

/**
 * Simple-interest term deposit maths, all in minor units (no floating point on money):
 * monthly interest = principal × rate ÷ 12, rounded half-up to the currency's minor unit.
 * Monthly payout pays it every month; AtMaturity pays the sum on the maturity date.
 */
class CalculateDepositUseCase(private val dateProvider: DateProvider) {

    operator fun invoke(deposit: TermDeposit): DepositDetail {
        val today = dateProvider.today()
        val start = deposit.startDate
        val maturity = start.plusMonths(deposit.termMonths)
        val monthly = monthlyInterestMinor(deposit.principal.minor, deposit.annualRateBp)
        val currency = deposit.principal.currency

        val schedule = when (deposit.payout) {
            InterestPayout.Monthly -> (1..deposit.termMonths).map { n ->
                val date = start.plusMonths(n)
                InterestPayment(n, date, Money(monthly, currency), isPaid = date <= today)
            }
            InterestPayout.AtMaturity -> listOf(
                InterestPayment(1, maturity, Money(monthly * deposit.termMonths, currency), isPaid = maturity <= today),
            )
        }
        val monthsElapsed = start.monthsUntil(today).coerceIn(0, deposit.termMonths)
        val totalDays = start.daysUntil(maturity).coerceAtLeast(1)
        val totalInterest = Money(monthly * deposit.termMonths, currency)

        return DepositDetail(
            deposit = deposit,
            maturityDate = maturity,
            status = if (today >= maturity) DepositStatus.Matured else DepositStatus.Active,
            daysToMaturity = today.daysUntil(maturity).coerceAtLeast(0),
            progress = (start.daysUntil(today).toFloat() / totalDays).coerceIn(0f, 1f),
            interestToDate = Money(monthly * monthsElapsed, currency),
            totalInterest = totalInterest,
            maturityAmount = deposit.principal + totalInterest,
            schedule = schedule,
        )
    }

    companion object {
        /** principal × bp ÷ 10 000 ÷ 12, rounded half-up. */
        fun monthlyInterestMinor(principalMinor: Long, annualRateBp: Int): Long =
            (principalMinor * annualRateBp + 60_000) / 120_000
    }
}

/** All deposits plus per-currency totals (USD and KHR are never added together). */
data class DepositPortfolio(
    val deposits: List<DepositDetail>,
    val principalByCurrency: Map<CurrencyCode, Money>,
    val interestToDateByCurrency: Map<CurrencyCode, Money>,
)

class GetDepositsUseCase(private val repository: DepositRepository, private val calculate: CalculateDepositUseCase) {
    /** Active deposits first, soonest maturity first. Totals count active deposits only. */
    suspend operator fun invoke(): Result<DepositPortfolio> = repository.getDeposits().map { list ->
        val details = list.map(calculate::invoke).sortedWith(compareBy({ it.status }, { it.maturityDate }))
        val active = details.filter { it.status == DepositStatus.Active }
        DepositPortfolio(
            deposits = details,
            principalByCurrency = active.sumByCurrency { it.deposit.principal },
            interestToDateByCurrency = active.sumByCurrency { it.interestToDate },
        )
    }

    private fun List<DepositDetail>.sumByCurrency(pick: (DepositDetail) -> Money): Map<CurrencyCode, Money> =
        groupBy { pick(it).currency }.mapValues { (_, items) -> items.map(pick).reduce(Money::plus) }
}

class GetDepositDetailUseCase(private val repository: DepositRepository, private val calculate: CalculateDepositUseCase) {
    suspend operator fun invoke(id: String): Result<DepositDetail> = repository.getDeposit(id).map(calculate::invoke)
}
