package com.example.m3showcase.feature.loans.domain.usecase

import com.example.m3showcase.core.common.DateProvider
import com.example.m3showcase.core.domain.model.Money
import com.example.m3showcase.core.domain.model.plusMonths
import com.example.m3showcase.feature.loans.domain.model.Installment
import com.example.m3showcase.feature.loans.domain.model.InstallmentStatus
import com.example.m3showcase.feature.loans.domain.model.Loan
import com.example.m3showcase.feature.loans.domain.model.LoanDetail
import com.example.m3showcase.feature.loans.domain.model.LoanPortfolio
import com.example.m3showcase.feature.loans.domain.repository.LoanRepository
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * Equal-installment (annuity) schedule:
 *   payment = P · r / (1 − (1 + r)^−n),  r = annual rate ÷ 12
 * Interest each month = balance · r, rounded half-up to the minor unit; the rest repays principal.
 * The last installment absorbs rounding so the balance ends at exactly zero.
 * Installments due before today count as paid (this demo has no missed payments).
 */
class CalculateLoanUseCase(private val dateProvider: DateProvider) {

    operator fun invoke(loan: Loan): LoanDetail {
        val today = dateProvider.today()
        val currency = loan.principal.currency
        val payment = monthlyPaymentMinor(loan.principal.minor, loan.annualRateBp, loan.termMonths)
        val rate = monthlyRate(loan.annualRateBp)

        var balance = loan.principal.minor
        var nextFound = false
        val schedule = (1..loan.termMonths).map { n ->
            val interest = BigDecimal(balance).multiply(rate).setScale(0, RoundingMode.HALF_UP).toLong()
            val isLast = n == loan.termMonths
            val principalPart = if (isLast) balance else (payment - interest).coerceAtMost(balance)
            balance -= principalPart
            val due = loan.startDate.plusMonths(n)
            val status = when {
                due < today -> InstallmentStatus.Paid
                !nextFound -> InstallmentStatus.Due.also { nextFound = true }
                else -> InstallmentStatus.Upcoming
            }
            Installment(
                number = n,
                dueDate = due,
                payment = Money(principalPart + interest, currency),
                principalPart = Money(principalPart, currency),
                interestPart = Money(interest, currency),
                balanceAfter = Money(balance, currency),
                status = status,
            )
        }
        val paid = schedule.filter { it.status == InstallmentStatus.Paid }
        return LoanDetail(
            loan = loan,
            monthlyPayment = Money(payment, currency),
            outstanding = paid.lastOrNull()?.balanceAfter ?: loan.principal,
            paidCount = paid.size,
            totalInterest = Money(schedule.sumOf { it.interestPart.minor }, currency),
            endDate = loan.startDate.plusMonths(loan.termMonths),
            nextInstallment = schedule.firstOrNull { it.status == InstallmentStatus.Due },
            schedule = schedule,
        )
    }

    companion object {
        private val MC = MathContext.DECIMAL128

        private fun monthlyRate(annualRateBp: Int): BigDecimal =
            BigDecimal(annualRateBp).divide(BigDecimal(120_000), MC)

        fun monthlyPaymentMinor(principalMinor: Long, annualRateBp: Int, termMonths: Int): Long {
            if (annualRateBp == 0) return BigDecimal(principalMinor).divide(BigDecimal(termMonths), 0, RoundingMode.HALF_UP).toLong()
            val r = monthlyRate(annualRateBp)
            val factor = BigDecimal.ONE.subtract(BigDecimal.ONE.add(r).pow(-termMonths, MC))
            return BigDecimal(principalMinor).multiply(r).divide(factor, 0, RoundingMode.HALF_UP).toLong()
        }
    }
}

class GetLoansUseCase(private val repository: LoanRepository, private val calculate: CalculateLoanUseCase) {
    suspend operator fun invoke(): Result<LoanPortfolio> = repository.getLoans().map { loans ->
        val details = loans.map(calculate::invoke).sortedBy { it.nextInstallment?.dueDate ?: it.endDate }
        LoanPortfolio(
            loans = details,
            outstandingByCurrency = details.filterNot { it.isPaidOff }
                .groupBy { it.outstanding.currency }
                .mapValues { (_, list) -> list.map { it.outstanding }.reduce(Money::plus) },
            nextPayment = details.mapNotNull { d -> d.nextInstallment?.let { d to it } }.minByOrNull { it.second.dueDate },
        )
    }
}

class GetLoanDetailUseCase(private val repository: LoanRepository, private val calculate: CalculateLoanUseCase) {
    suspend operator fun invoke(id: String): Result<LoanDetail> = repository.getLoan(id).map(calculate::invoke)
}
