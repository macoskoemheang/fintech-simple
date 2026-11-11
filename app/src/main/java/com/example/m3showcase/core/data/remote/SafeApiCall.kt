package com.example.m3showcase.core.data.remote

import com.example.m3showcase.core.data.remote.dto.ApiException
import com.example.m3showcase.core.domain.model.BankError
import kotlinx.coroutines.CancellationException
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * The single place where transport and API exceptions become [BankError].
 * CancellationException is rethrown so structured concurrency keeps working.
 */
suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: SocketTimeoutException) {
    Result.failure(BankError.Timeout)
} catch (e: IOException) {
    Result.failure(BankError.NoInternet)
} catch (e: ApiException) {
    Result.failure(e.toBankError())
} catch (e: Exception) {
    Result.failure(BankError.Unknown(e.message))
}

private fun ApiException.toBankError(): BankError = when (errorCode) {
    "INVALID_PIN" -> BankError.InvalidPin
    "INSUFFICIENT_FUNDS" -> BankError.InsufficientFunds
    "ACCOUNT_NOT_FOUND" -> BankError.AccountNotFound
    "SAME_ACCOUNT" -> BankError.Validation(BankError.Field.AccountNumber, "You can't transfer to the same account")
    "CURRENCY_MISMATCH" -> BankError.Validation(BankError.Field.AccountNumber, "Destination account uses a different currency")
    else -> BankError.Unknown(message)
}
