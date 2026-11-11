package com.example.m3showcase.core.di

import com.example.m3showcase.core.common.DefaultDispatcherProvider
import com.example.m3showcase.core.common.DispatcherProvider
import com.example.m3showcase.core.common.CambodiaDateProvider
import com.example.m3showcase.core.common.DateProvider
import com.example.m3showcase.core.data.repository.StaticFestivalCalendarRepository
import com.example.m3showcase.core.domain.repository.FestivalCalendarRepository
import com.example.m3showcase.core.domain.usecase.GetActiveFestivalUseCase
import com.example.m3showcase.core.domain.usecase.GetFestivalScheduleUseCase
import com.example.m3showcase.core.domain.usecase.ResolveEventThemeUseCase
import com.example.m3showcase.core.domain.usecase.SetEventThemeUseCase
import com.example.m3showcase.core.connectivity.AndroidConnectivityObserver
import com.example.m3showcase.core.connectivity.ConnectivityObserver
import com.example.m3showcase.core.connectivity.NetworkMonitor
import com.example.m3showcase.core.data.local.BankLocalDataSource
import com.example.m3showcase.core.data.local.SettingsLocalDataSource
import com.example.m3showcase.core.data.remote.FakeBankRemoteDataSource
import com.example.m3showcase.core.data.repository.AccountRepositoryImpl
import com.example.m3showcase.core.data.repository.AuthRepositoryImpl
import com.example.m3showcase.core.data.repository.SettingsRepositoryImpl
import com.example.m3showcase.core.data.repository.TransactionRepositoryImpl
import com.example.m3showcase.core.data.repository.TransferRepositoryImpl
import com.example.m3showcase.core.domain.repository.AccountRepository
import com.example.m3showcase.core.domain.repository.AuthRepository
import com.example.m3showcase.core.domain.repository.SettingsRepository
import com.example.m3showcase.core.domain.repository.TransactionRepository
import com.example.m3showcase.core.domain.repository.TransferRepository
import com.example.m3showcase.core.domain.usecase.GetTransactionUseCase
import com.example.m3showcase.core.domain.usecase.LogoutUseCase
import com.example.m3showcase.core.domain.usecase.ObserveAccountsUseCase
import com.example.m3showcase.core.domain.usecase.ObserveNetworkStatusUseCase
import com.example.m3showcase.core.domain.usecase.ObserveSessionUseCase
import com.example.m3showcase.core.domain.usecase.ObserveSettingsUseCase
import com.example.m3showcase.core.domain.usecase.RefreshBankDataUseCase
import com.example.m3showcase.core.domain.usecase.SetBalanceHiddenUseCase
import com.example.m3showcase.core.domain.usecase.SetThemeModeUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Lives as long as the process; for app-wide HOT flows like [NetworkMonitor]. */
val AppScope = named("AppScope")

val coreModule = module {
    single<DispatcherProvider> { DefaultDispatcherProvider() }
    single<DateProvider> { CambodiaDateProvider() }
    single(AppScope) { CoroutineScope(SupervisorJob() + get<DispatcherProvider>().default) }

    // Connectivity: one cold source → one shared hot StateFlow
    single<ConnectivityObserver> { AndroidConnectivityObserver(androidContext()) }
    single { NetworkMonitor(observer = get(), appScope = get(AppScope)) }

    // Data sources
    singleOf(::FakeBankRemoteDataSource)
    singleOf(::BankLocalDataSource)
    single { SettingsLocalDataSource(androidContext()) }

    // Repositories (domain interface ← data implementation)
    singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }
    singleOf(::AccountRepositoryImpl) { bind<AccountRepository>() }
    singleOf(::TransactionRepositoryImpl) { bind<TransactionRepository>() }
    singleOf(::TransferRepositoryImpl) { bind<TransferRepository>() }
    singleOf(::SettingsRepositoryImpl) { bind<SettingsRepository>() }
    singleOf(::StaticFestivalCalendarRepository) { bind<FestivalCalendarRepository>() }

    // Shared use cases. factory: they hold no state, so a new instance each time is fine.
    factoryOf(::ObserveAccountsUseCase)
    factoryOf(::RefreshBankDataUseCase)
    factoryOf(::GetTransactionUseCase)
    factoryOf(::ObserveSettingsUseCase)
    factoryOf(::SetThemeModeUseCase)
    factoryOf(::SetBalanceHiddenUseCase)
    factoryOf(::SetEventThemeUseCase)
    factoryOf(::GetActiveFestivalUseCase)
    factoryOf(::ResolveEventThemeUseCase)
    factoryOf(::GetFestivalScheduleUseCase)
    factoryOf(::ObserveSessionUseCase)
    factoryOf(::LogoutUseCase)
    factoryOf(::ObserveNetworkStatusUseCase)
}
