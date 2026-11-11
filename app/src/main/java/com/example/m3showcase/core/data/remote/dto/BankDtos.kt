package com.example.m3showcase.core.data.remote.dto

/*
 * Network DTOs: they mirror the API's JSON 1:1 and never leave the data layer.
 * With a real backend these would carry @Serializable / @SerialName annotations.
 */

data class UserDto(
    val id: String,
    val fullName: String,
    val phone: String,
)

data class AccountDto(
    val id: String,
    val name: String,
    val accountNumber: String,
    val type: String,
    val balanceMinor: Long,
    val currency: String,
    val isDefault: Boolean,
)

data class TransactionDto(
    val id: String,
    val accountId: String,
    val title: String,
    val counterparty: String,
    val amountMinor: Long,
    val currency: String,
    val category: String,
    val timestamp: Long,
    val note: String? = null,
)

data class TransferRequestDto(
    val fromAccountId: String,
    val toAccountNumber: String,
    val amountMinor: Long,
    val currency: String,
    val note: String?,
)

data class TransferResponseDto(
    val debit: TransactionDto,
    val credit: TransactionDto?,
    val updatedAccounts: List<AccountDto>,
)

/** Error body returned by the API, e.g. HTTP 422 + "INSUFFICIENT_FUNDS". */
class ApiException(val httpCode: Int, val errorCode: String) : Exception("$httpCode $errorCode")
