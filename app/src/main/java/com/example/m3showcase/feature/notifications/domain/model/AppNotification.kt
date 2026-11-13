package com.example.m3showcase.feature.notifications.domain.model

data class AppNotification(
    val id: String,
    val title: String,
    val body: String,
    val category: NotificationCategory,
    val timestamp: Long,
    val isRead: Boolean,
)

enum class NotificationCategory { Transaction, Security, Promotion, System }

/** Server-side filters. Changing one starts paging again from page one. */
enum class NotificationFilter { All, Unread, Transactions, Security, Promotions }
