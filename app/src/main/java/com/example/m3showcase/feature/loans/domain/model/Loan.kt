package com.example.m3showcase.feature.loans.domain.model

import com.example.m3showcase.core.domain.model.CalendarDate
import com.example.m3showcase.core.domain.model.CurrencyCode
import com.example.m3showcase.core.domain.model.Money

/** A loan as the bank stores it. The schedule and balances are derived in [LoanDetail]. */
data class Loan(
    val id: String,
    val name: String,
    val number: String,
    val type: LoanType,
    val principal: Money,
    /** Annual rate in basis points: 950 = 9.50% p.a. */
    val annualRateBp: Int,
    val termMonths: Int,
    /** Disbursement date; the first installment is due one month later. */
    val startDate: CalendarDate,
)

enum class LoanType { Personal, Home, Car, Business }

enum class InstallmentStatus { Paid, Due, Upcoming }

data class Installment(
    val number: Int,
    val dueDate: CalendarDate,
    val payment: Money,
    val principalPart: Money,
    val interestPart: Money,
    val balanceAfter: Money,
    val status: InstallmentStatus,
)

data class LoanDetail(
    val loan: Loan,
    val monthlyPayment: Money,
    val outstanding: Money,
    val paidCount: Int,
    val totalInterest: Money,
    val endDate: CalendarDate,
    val nextInstallment: Installment?,
    val schedule: List<Installment>,
) {
    /** Share of the principal already repaid, 0..1. */
    val progress: Float get() = 1f - outstanding.minor.toFloat() / loan.principal.minor.coerceAtLeast(1)
    val isPaidOff: Boolean get() = nextInstallment == null
}

data class LoanPortfolio(
    val loans: List<LoanDetail>,
    val outstandingByCurrency: Map<CurrencyCode, Money>,
    /** The soonest unpaid installment across all loans. */
    val nextPayment: Pair<LoanDetail, Installment>?,
)
