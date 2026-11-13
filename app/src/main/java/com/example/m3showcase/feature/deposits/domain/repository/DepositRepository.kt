package com.example.m3showcase.feature.deposits.domain.repository

import com.example.m3showcase.feature.deposits.domain.model.TermDeposit

interface DepositRepository {
    suspend fun getDeposits(): Result<List<TermDeposit>>
    suspend fun getDeposit(id: String): Result<TermDeposit>
}
