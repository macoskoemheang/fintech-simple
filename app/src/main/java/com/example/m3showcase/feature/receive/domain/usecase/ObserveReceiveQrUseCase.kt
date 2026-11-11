package com.example.m3showcase.feature.receive.domain.usecase

import com.example.m3showcase.core.domain.model.ReceiveQr
import com.example.m3showcase.core.domain.usecase.ObserveAccountsUseCase
import com.example.m3showcase.core.domain.usecase.ObserveSessionUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.net.URLEncoder

/**
 * Builds one receive-QR payload per account. A production app would use the
 * national KHQR/EMVCo format here; the rest of the app would stay the same.
 */
class ObserveReceiveQrUseCase(
    private val observeAccounts: ObserveAccountsUseCase,
    private val observeSession: ObserveSessionUseCase,
) {
    operator fun invoke(): Flow<List<ReceiveQr>> = combine(observeAccounts(), observeSession()) { accounts, user ->
        val name = user?.fullName?.uppercase().orEmpty()
        accounts.map { account ->
            ReceiveQr(
                accountName = name,
                accountNumber = account.number,
                currency = account.balance.currency,
                payload = "novabank://pay?acc=${account.number}" +
                    "&name=${URLEncoder.encode(name, "UTF-8")}&cur=${account.balance.currency.name}",
            )
        }
    }
}
