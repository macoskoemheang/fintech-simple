package com.example.m3showcase.feature.notifications.domain.repository

import com.example.m3showcase.core.domain.model.Page
import com.example.m3showcase.core.domain.model.PageRequest
import com.example.m3showcase.feature.notifications.domain.model.AppNotification
import com.example.m3showcase.feature.notifications.domain.model.NotificationFilter

/**
 * Notifications are paged straight from the network (no local cache): the inbox can be long,
 * and only the first pages are ever looked at.
 */
interface NotificationRepository {
    suspend fun getPage(filter: NotificationFilter, request: PageRequest): Result<Page<AppNotification>>
    suspend fun markAsRead(id: String): Result<Unit>
    suspend fun markAllAsRead(): Result<Unit>
}
