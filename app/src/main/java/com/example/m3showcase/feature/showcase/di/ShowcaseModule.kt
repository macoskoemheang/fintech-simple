package com.example.m3showcase.feature.showcase.di

import com.example.m3showcase.feature.showcase.data.ShowcaseRepositoryImpl
import com.example.m3showcase.feature.showcase.domain.repository.ShowcaseRepository
import com.example.m3showcase.feature.showcase.domain.usecase.GetShowcaseContentUseCase
import com.example.m3showcase.feature.showcase.presentation.ShowcaseViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val showcaseModule = module {
    single<ShowcaseRepository> { ShowcaseRepositoryImpl() }
    factory { GetShowcaseContentUseCase(get()) }
    viewModelOf(::ShowcaseViewModel)
}
