package com.example.m3showcase.feature.auth.domain.usecase

import com.example.m3showcase.core.domain.model.BankError
import com.example.m3showcase.core.domain.model.UserProfile
import com.example.m3showcase.core.domain.repository.AuthRepository

class LoginWithPinUseCase(private val repository: AuthRepository) {

    suspend operator fun invoke(pin: String): Result<UserProfile> {
        if (pin.length != PIN_LENGTH || !pin.all(Char::isDigit)) {
            return Result.failure(BankError.Validation(BankError.Field.Pin, BankError.Rule.WrongLength, PIN_LENGTH))
        }
        return repository.login(pin)
    }

    companion object {
        const val PIN_LENGTH = 4
    }
}
