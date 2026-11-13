package com.example.m3showcase.feature.notifications.di

import com.example.m3showcase.feature.notifications.data.FakeNotificationRemoteDataSource
import com.example.m3showcase.feature.notifications.data.NotificationRepositoryImpl
import com.example.m3showcase.feature.notifications.domain.repository.NotificationRepository
import com.example.m3showcase.feature.notifications.domain.usecase.GetNotificationsPageUseCase
import com.example.m3showcase.feature.notifications.domain.usecase.MarkAllNotificationsReadUseCase
import com.example.m3showcase.feature.notifications.domain.usecase.MarkNotificationReadUseCase
import com.example.m3showcase.feature.notifications.presentation.NotificationsViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val notificationsModule = module {
    // single: the fake "server" keeps read state for the whole process.
    singleOf(::FakeNotificationRemoteDataSource)
    singleOf(::NotificationRepositoryImpl) { bind<NotificationRepository>() }
    factoryOf(::GetNotificationsPageUseCase)
    factoryOf(::MarkNotificationReadUseCase)
    factoryOf(::MarkAllNotificationsReadUseCase)
    viewModelOf(::NotificationsViewModel)
}
