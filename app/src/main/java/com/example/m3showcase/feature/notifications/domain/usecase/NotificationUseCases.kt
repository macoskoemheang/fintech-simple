package com.example.m3showcase.feature.notifications.domain.usecase

import com.example.m3showcase.core.domain.model.Page
import com.example.m3showcase.core.domain.model.PageRequest
import com.example.m3showcase.feature.notifications.domain.model.AppNotification
import com.example.m3showcase.feature.notifications.domain.model.NotificationFilter
import com.example.m3showcase.feature.notifications.domain.repository.NotificationRepository

class GetNotificationsPageUseCase(private val repository: NotificationRepository) {
    suspend operator fun invoke(filter: NotificationFilter, request: PageRequest): Result<Page<AppNotification>> =
        repository.getPage(filter, request)
}

class MarkNotificationReadUseCase(private val repository: NotificationRepository) {
    suspend operator fun invoke(id: String): Result<Unit> = repository.markAsRead(id)
}

class MarkAllNotificationsReadUseCase(private val repository: NotificationRepository) {
    suspend operator fun invoke(): Result<Unit> = repository.markAllAsRead()
}
