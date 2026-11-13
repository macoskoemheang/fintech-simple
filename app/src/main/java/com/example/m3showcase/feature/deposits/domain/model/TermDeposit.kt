package com.example.m3showcase.feature.deposits.domain.model

import com.example.m3showcase.core.domain.model.CalendarDate
import com.example.m3showcase.core.domain.model.Money

/** A fixed (term) deposit as the bank stores it. Derived numbers live in [DepositDetail]. */
data class TermDeposit(
    val id: String,
    val name: String,
    val number: String,
    val principal: Money,
    /** Annual simple-interest rate in basis points: 525 = 5.25% p.a. */
    val annualRateBp: Int,
    val termMonths: Int,
    val startDate: CalendarDate,
    val payout: InterestPayout,
    val autoRenew: Boolean,
)

enum class InterestPayout { Monthly, AtMaturity }

enum class DepositStatus { Active, Matured }

data class InterestPayment(val number: Int, val date: CalendarDate, val amount: Money, val isPaid: Boolean)

/** A deposit with everything the UI shows, computed by [com.example.m3showcase.feature.deposits.domain.usecase.CalculateDepositUseCase]. */
data class DepositDetail(
    val deposit: TermDeposit,
    val maturityDate: CalendarDate,
    val status: DepositStatus,
    val daysToMaturity: Long,
    /** 0..1 through the term, by days. */
    val progress: Float,
    /** Interest accrued for the full months already passed. */
    val interestToDate: Money,
    val totalInterest: Money,
    val maturityAmount: Money,
    val schedule: List<InterestPayment>,
)
