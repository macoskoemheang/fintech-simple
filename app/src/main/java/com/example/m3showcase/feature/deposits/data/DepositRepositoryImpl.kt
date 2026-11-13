package com.example.m3showcase.feature.deposits.data

import com.example.m3showcase.core.data.remote.safeApiCall
import com.example.m3showcase.core.domain.model.CurrencyCode
import com.example.m3showcase.core.domain.model.Money
import com.example.m3showcase.core.domain.model.parseIsoDate
import com.example.m3showcase.feature.deposits.domain.model.InterestPayout
import com.example.m3showcase.feature.deposits.domain.model.TermDeposit
import com.example.m3showcase.feature.deposits.domain.repository.DepositRepository

class DepositRepositoryImpl(private val remote: FakeDepositRemoteDataSource) : DepositRepository {
    override suspend fun getDeposits() = safeApiCall { remote.getDeposits().map { it.toDomain() } }
    override suspend fun getDeposit(id: String) = safeApiCall { remote.getDeposit(id).toDomain() }
}

private fun DepositDto.toDomain() = TermDeposit(
    id = id,
    name = productName,
    number = accountNumber.filter(Char::isDigit),
    principal = Money(principalMinor, CurrencyCode.valueOf(currency)),
    annualRateBp = rateBp,
    termMonths = termMonths,
    startDate = parseIsoDate(startDate),
    payout = if (payout == "MONTHLY") InterestPayout.Monthly else InterestPayout.AtMaturity,
    autoRenew = autoRenew,
)
