package com.example.m3showcase.feature.loans.data

import com.example.m3showcase.core.data.remote.safeApiCall
import com.example.m3showcase.core.domain.model.CurrencyCode
import com.example.m3showcase.core.domain.model.Money
import com.example.m3showcase.core.domain.model.parseIsoDate
import com.example.m3showcase.feature.loans.domain.model.Loan
import com.example.m3showcase.feature.loans.domain.model.LoanType
import com.example.m3showcase.feature.loans.domain.repository.LoanRepository

class LoanRepositoryImpl(private val remote: FakeLoanRemoteDataSource) : LoanRepository {
    override suspend fun getLoans() = safeApiCall { remote.getLoans().map { it.toDomain() } }
    override suspend fun getLoan(id: String) = safeApiCall { remote.getLoan(id).toDomain() }
}

private fun LoanDto.toDomain() = Loan(
    id = id,
    name = productName,
    number = loanNumber.filter(Char::isDigit),
    type = LoanType.entries.firstOrNull { it.name.equals(type, ignoreCase = true) } ?: LoanType.Personal,
    principal = Money(principalMinor, CurrencyCode.valueOf(currency)),
    annualRateBp = rateBp,
    termMonths = termMonths,
    startDate = parseIsoDate(disbursedOn),
)
