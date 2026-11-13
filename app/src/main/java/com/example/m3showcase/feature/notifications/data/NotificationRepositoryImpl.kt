package com.example.m3showcase.feature.notifications.data

import com.example.m3showcase.core.data.remote.safeApiCall
import com.example.m3showcase.core.domain.model.Page
import com.example.m3showcase.core.domain.model.PageRequest
import com.example.m3showcase.feature.notifications.domain.model.AppNotification
import com.example.m3showcase.feature.notifications.domain.model.NotificationCategory
import com.example.m3showcase.feature.notifications.domain.model.NotificationFilter
import com.example.m3showcase.feature.notifications.domain.repository.NotificationRepository

class NotificationRepositoryImpl(private val remote: FakeNotificationRemoteDataSource) : NotificationRepository {

    /** The API pages by timestamp; the cursor carries it as text, so the domain stays API-agnostic. */
    override suspend fun getPage(filter: NotificationFilter, request: PageRequest): Result<Page<AppNotification>> = safeApiCall {
        val dto = remote.getNotifications(before = request.cursor?.toLongOrNull(), limit = request.size, filter = filter.toApi())
        Page(items = dto.data.map { it.toDomain() }, nextCursor = dto.nextBefore?.toString(), totalCount = dto.total)
    }

    override suspend fun markAsRead(id: String): Result<Unit> = safeApiCall { remote.markRead(id) }

    override suspend fun markAllAsRead(): Result<Unit> = safeApiCall { remote.markAllRead() }
}

private fun NotificationFilter.toApi() = when (this) {
    NotificationFilter.All -> "ALL"
    NotificationFilter.Unread -> "UNREAD"
    NotificationFilter.Transactions -> "TRANSACTION"
    NotificationFilter.Security -> "SECURITY"
    NotificationFilter.Promotions -> "PROMOTION"
}

private fun NotificationDto.toDomain() = AppNotification(
    id = id,
    title = title,
    body = body,
    category = when (category) {
        "TRANSACTION" -> NotificationCategory.Transaction
        "SECURITY" -> NotificationCategory.Security
        "PROMOTION" -> NotificationCategory.Promotion
        else -> NotificationCategory.System
    },
    timestamp = createdAt,
    isRead = read,
)
