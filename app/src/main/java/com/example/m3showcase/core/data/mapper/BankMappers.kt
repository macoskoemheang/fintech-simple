package com.example.m3showcase.core.data.mapper

import com.example.m3showcase.core.data.remote.dto.AccountDto
import com.example.m3showcase.core.data.remote.dto.TransactionDto
import com.example.m3showcase.core.data.remote.dto.TransferRequestDto
import com.example.m3showcase.core.data.remote.dto.UserDto
import com.example.m3showcase.core.domain.model.Account
import com.example.m3showcase.core.domain.model.AccountType
import com.example.m3showcase.core.domain.model.CurrencyCode
import com.example.m3showcase.core.domain.model.Money
import com.example.m3showcase.core.domain.model.Transaction
import com.example.m3showcase.core.domain.model.TransactionCategory
import com.example.m3showcase.core.domain.model.TransferRequest
import com.example.m3showcase.core.domain.model.UserProfile

fun UserDto.toDomain() = UserProfile(id = id, fullName = fullName, phoneNumber = phone)

fun AccountDto.toDomain() = Account(
    id = id,
    name = name,
    number = accountNumber,
    type = when (type) {
        "CURRENT" -> AccountType.Current
        "FIXED" -> AccountType.FixedDeposit
        else -> AccountType.Savings
    },
    balance = Money(balanceMinor, currency.toCurrency()),
    isDefault = isDefault,
)

fun TransactionDto.toDomain() = Transaction(
    id = id,
    accountId = accountId,
    title = title,
    counterparty = counterparty,
    amount = Money(amountMinor, currency.toCurrency()),
    category = when (category) {
        "PAYMENT" -> TransactionCategory.Payment
        "TOP_UP" -> TransactionCategory.TopUp
        "QR_PAYMENT" -> TransactionCategory.QrPayment
        "SALARY" -> TransactionCategory.Salary
        "CASH" -> TransactionCategory.Cash
        else -> TransactionCategory.Transfer
    },
    timestampMillis = timestamp,
    note = note,
)

fun TransferRequest.toDto() = TransferRequestDto(
    fromAccountId = fromAccountId,
    toAccountNumber = toAccountNumber,
    amountMinor = amount.minor,
    currency = amount.currency.name,
    note = note,
)

private fun String.toCurrency(): CurrencyCode =
    CurrencyCode.entries.firstOrNull { it.name == this } ?: CurrencyCode.USD
