package com.example.m3showcase.feature.loans.domain.repository

import com.example.m3showcase.feature.loans.domain.model.Loan

interface LoanRepository {
    suspend fun getLoans(): Result<List<Loan>>
    suspend fun getLoan(id: String): Result<Loan>
}
