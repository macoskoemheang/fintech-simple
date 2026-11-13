package com.example.m3showcase.feature.payments.domain.model

import com.example.m3showcase.core.domain.model.Money

/** A bill payment made from one of the user's accounts. */
data class Payment(
    val id: String,
    val biller: String,
    val category: BillerCategory,
    /** The customer's id at the biller (meter number, phone number, student id…). */
    val customerRef: String,
    val amount: Money,
    val fee: Money,
    val status: PaymentStatus,
    val timestamp: Long,
    val fromAccountName: String,
    val fromAccountNumber: String,
    val reference: String,
) {
    val total: Money get() = amount + fee
}

enum class BillerCategory { Electricity, Water, Internet, Mobile, Education, Insurance }

enum class PaymentStatus { Success, Pending, Failed }

data class PaymentSummary(val paidThisMonth: Money, val paymentsThisMonth: Int, val pendingCount: Int)
