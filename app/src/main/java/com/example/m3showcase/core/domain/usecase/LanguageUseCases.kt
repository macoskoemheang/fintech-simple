package com.example.m3showcase.core.domain.usecase

import com.example.m3showcase.core.domain.model.AppLanguage
import com.example.m3showcase.core.domain.repository.LanguageRepository
import kotlinx.coroutines.flow.StateFlow

class ObserveAppLanguageUseCase(private val repository: LanguageRepository) {
    operator fun invoke(): StateFlow<AppLanguage> = repository.language
}

class SetAppLanguageUseCase(private val repository: LanguageRepository) {
    operator fun invoke(language: AppLanguage) = repository.setLanguage(language)
}
