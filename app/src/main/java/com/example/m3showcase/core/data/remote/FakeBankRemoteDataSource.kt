package com.example.m3showcase.core.data.remote

import com.example.m3showcase.core.common.DispatcherProvider
import com.example.m3showcase.core.connectivity.NetworkMonitor
import com.example.m3showcase.core.data.remote.dto.AccountDto
import com.example.m3showcase.core.data.remote.dto.ApiException
import com.example.m3showcase.core.data.remote.dto.TransactionDto
import com.example.m3showcase.core.data.remote.dto.TransferRequestDto
import com.example.m3showcase.core.data.remote.dto.TransferResponseDto
import com.example.m3showcase.core.data.remote.dto.UserDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Stand-in for a Retrofit service. It keeps "server" state in memory, adds latency,
 * and throws IOException when the device is offline, so the app's connectivity and
 * error handling run as they would against a real API.
 *
 * To use a real backend, replace this class with a Retrofit-backed one that has the
 * same functions. Nothing above the data layer changes.
 */
class FakeBankRemoteDataSource(
    private val networkMonitor: NetworkMonitor,
    private val dispatchers: DispatcherProvider,
) {
    private val mutex = Mutex()
    private val user = UserDto(id = "u-001", fullName = "Sok Dara", phone = "012 345 678")
    private val accounts = seedAccounts()
    private val transactions = seedTransactions()

    suspend fun login(pin: String): UserDto = request {
        if (pin != DEMO_PIN) throw ApiException(401, "INVALID_PIN")
        user
    }

    suspend fun getAccounts(): List<AccountDto> = request { accounts.toList() }

    suspend fun getTransactions(): List<TransactionDto> = request {
        transactions.sortedByDescending { it.timestamp }
    }

    suspend fun transfer(body: TransferRequestDto): TransferResponseDto = request(latencyMs = 1_200) {
        val fromIndex = accounts.indexOfFirst { it.id == body.fromAccountId }
        if (fromIndex < 0) throw ApiException(404, "ACCOUNT_NOT_FOUND")
        val from = accounts[fromIndex]
        if (from.accountNumber == body.toAccountNumber) throw ApiException(400, "SAME_ACCOUNT")
        if (from.currency != body.currency) throw ApiException(400, "CURRENCY_MISMATCH")
        if (from.balanceMinor < body.amountMinor) throw ApiException(422, "INSUFFICIENT_FUNDS")

        val now = System.currentTimeMillis()
        val updated = mutableListOf(from.copy(balanceMinor = from.balanceMinor - body.amountMinor))
        accounts[fromIndex] = updated.first()

        // Transfer between own accounts: credit the destination as well.
        val toIndex = accounts.indexOfFirst { it.accountNumber == body.toAccountNumber }
        val credit = if (toIndex >= 0) {
            val to = accounts[toIndex]
            if (to.currency != body.currency) throw ApiException(400, "CURRENCY_MISMATCH")
            accounts[toIndex] = to.copy(balanceMinor = to.balanceMinor + body.amountMinor)
            updated += accounts[toIndex]
            TransactionDto(
                id = newId(), accountId = to.id, title = "Received from own account",
                counterparty = from.accountNumber, amountMinor = body.amountMinor,
                currency = body.currency, category = "TRANSFER", timestamp = now, note = body.note,
            ).also { transactions += it }
        } else {
            null
        }

        val debit = TransactionDto(
            id = newId(), accountId = from.id, title = "Transfer to ${body.toAccountNumber}",
            counterparty = if (toIndex >= 0) accounts[toIndex].name else recipientName(body.toAccountNumber),
            amountMinor = -body.amountMinor,
            currency = body.currency, category = "TRANSFER", timestamp = now, note = body.note,
        )
        transactions += debit
        TransferResponseDto(debit = debit, credit = credit, updatedAccounts = updated)
    }

    /** Simulates one HTTP round-trip: offline check, latency, then thread-safe "server" work. */
    private suspend fun <T> request(latencyMs: Long = 700, block: () -> T): T =
        withContext(dispatchers.io) {
            if (!networkMonitor.isOnline) throw IOException("Unable to resolve host")
            delay(latencyMs)
            if (!networkMonitor.isOnline) throw IOException("Connection reset")
            mutex.withLock { block() }
        }

    private fun newId() = UUID.randomUUID().toString()

    private fun recipientName(accountNumber: String) = when (accountNumber.takeLast(1)) {
        "1", "2" -> "Chan Sopheap"
        "3", "4" -> "Lim Vanna"
        "5", "6" -> "Keo Ratana"
        else -> "Meas Sreyneang"
    }

    private fun seedAccounts() = mutableListOf(
        AccountDto("acc-usd", "Savings USD", "000123456", "SAVINGS", 1_254_075, "USD", isDefault = true),
        AccountDto("acc-khr", "Savings KHR", "000654321", "SAVINGS", 2_450_000, "KHR", isDefault = false),
        AccountDto("acc-cur", "Business USD", "000789012", "CURRENT", 532_010, "USD", isDefault = false),
    )

    private fun seedTransactions(): MutableList<TransactionDto> {
        val now = System.currentTimeMillis()
        fun ago(hours: Long) = now - TimeUnit.HOURS.toMillis(hours)
        return mutableListOf(
            TransactionDto(newId(), "acc-usd", "Salary October", "ACME Co., Ltd.", 180_000, "USD", "SALARY", ago(3)),
            TransactionDto(newId(), "acc-usd", "Coffee", "Brown Coffee BKK1", -375, "USD", "QR_PAYMENT", ago(5)),
            TransactionDto(newId(), "acc-usd", "Mobile top-up", "Smart Axiata", -500, "USD", "TOP_UP", ago(26)),
            TransactionDto(newId(), "acc-khr", "Market", "Phsar Thmey Shop", -48_000, "KHR", "QR_PAYMENT", ago(30)),
            TransactionDto(newId(), "acc-usd", "Electricity bill", "EDC", -4_250, "USD", "PAYMENT", ago(50)),
            TransactionDto(newId(), "acc-cur", "Invoice #1042", "Lim Vanna", 25_000, "USD", "TRANSFER", ago(72)),
            TransactionDto(newId(), "acc-khr", "Received", "Keo Ratana", 200_000, "KHR", "TRANSFER", ago(96)),
            TransactionDto(newId(), "acc-usd", "ATM withdrawal", "ATM Norodom", -10_000, "USD", "CASH", ago(120)),
        )
    }

    companion object {
        /** Demo PIN for the fake backend. */
        const val DEMO_PIN = "1234"
    }
}
