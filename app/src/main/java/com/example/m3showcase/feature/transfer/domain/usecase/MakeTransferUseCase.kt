package com.example.m3showcase.feature.transfer.domain.usecase

import com.example.m3showcase.core.domain.model.Transaction
import com.example.m3showcase.core.domain.model.TransferRequest
import com.example.m3showcase.core.domain.repository.TransferRepository

class MakeTransferUseCase(private val repository: TransferRepository) {
    suspend operator fun invoke(request: TransferRequest): Result<Transaction> = repository.transfer(request)
}
