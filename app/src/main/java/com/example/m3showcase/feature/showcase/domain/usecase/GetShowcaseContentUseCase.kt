package com.example.m3showcase.feature.showcase.domain.usecase

import com.example.m3showcase.feature.showcase.domain.model.ShowcaseContent
import com.example.m3showcase.feature.showcase.domain.repository.ShowcaseRepository

class GetShowcaseContentUseCase(private val repository: ShowcaseRepository) {
    suspend operator fun invoke(): Result<ShowcaseContent> = repository.getContent()
}
