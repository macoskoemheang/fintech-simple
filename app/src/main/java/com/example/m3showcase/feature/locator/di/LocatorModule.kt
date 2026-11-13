package com.example.m3showcase.feature.locator.di

import com.example.m3showcase.feature.locator.data.FakeLocationRemoteDataSource
import com.example.m3showcase.feature.locator.data.LocationRepositoryImpl
import com.example.m3showcase.feature.locator.domain.repository.LocationRepository
import com.example.m3showcase.feature.locator.domain.usecase.FilterLocationsUseCase
import com.example.m3showcase.feature.locator.domain.usecase.GetNearbyLocationsUseCase
import com.example.m3showcase.feature.locator.presentation.LocatorViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val locatorModule = module {
    singleOf(::FakeLocationRemoteDataSource)
    singleOf(::LocationRepositoryImpl) { bind<LocationRepository>() }
    factoryOf(::GetNearbyLocationsUseCase)
    factoryOf(::FilterLocationsUseCase)
    viewModelOf(::LocatorViewModel)
}
