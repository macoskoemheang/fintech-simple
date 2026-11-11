package com.example.m3showcase.feature.transfer

import com.example.m3showcase.core.domain.model.Account
import com.example.m3showcase.core.domain.model.AccountType
import com.example.m3showcase.core.domain.model.BankError
import com.example.m3showcase.core.domain.model.CurrencyCode
import com.example.m3showcase.core.domain.model.Money
import com.example.m3showcase.feature.transfer.domain.usecase.ValidateTransferUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateTransferUseCaseTest {

    private val validate = ValidateTransferUseCase()
    private val usd = Account("a1", "Savings", "000123456", AccountType.Savings, Money(10_000, CurrencyCode.USD), true)
    private val khr = Account("a2", "Riel", "000654321", AccountType.Savings, Money(50_000, CurrencyCode.KHR), false)

    @Test
    fun `valid USD amount converts to cents`() {
        val request = validate(usd, "000 999 111", "12.50", "  lunch ").getOrThrow()
        assertEquals(Money(1_250, CurrencyCode.USD), request.amount)
        assertEquals("000999111", request.toAccountNumber)
        assertEquals("lunch", request.note)
    }

    @Test
    fun `KHR does not allow decimals`() = assertField(validate(khr, "000999111", "100.5", ""), BankError.Field.Amount)

    @Test
    fun `amount above balance fails`() = assertField(validate(usd, "000999111", "100.01", ""), BankError.Field.Amount)

    @Test
    fun `zero amount fails`() = assertField(validate(usd, "000999111", "0", ""), BankError.Field.Amount)

    @Test
    fun `short account number fails`() = assertField(validate(usd, "1234", "1", ""), BankError.Field.AccountNumber)

    @Test
    fun `same account fails`() = assertField(validate(usd, usd.number, "1", ""), BankError.Field.AccountNumber)

    @Test
    fun `missing source account fails`() = assertField(validate(null, "000999111", "1", ""), BankError.Field.FromAccount)

    private fun assertField(result: Result<*>, field: BankError.Field) {
        val error = result.exceptionOrNull()
        assertTrue("Expected validation error, got $error", error is BankError.Validation)
        assertEquals(field, (error as BankError.Validation).field)
    }
}
