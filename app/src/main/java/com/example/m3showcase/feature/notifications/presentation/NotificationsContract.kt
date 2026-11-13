package com.example.m3showcase.feature.notifications.presentation

import androidx.compose.runtime.Immutable
import com.example.m3showcase.core.paging.PagingState
import com.example.m3showcase.feature.notifications.domain.model.AppNotification
import com.example.m3showcase.feature.notifications.domain.model.NotificationFilter
import com.example.m3showcase.core.ui.UiText

/** One state for the whole screen; list load states live in [paging]. */
@Immutable
data class NotificationsViewState(
    val filter: NotificationFilter = NotificationFilter.All,
    val paging: PagingState<AppNotification> = PagingState(),
    val isMarkingAllRead: Boolean = false,
) {
    val hasUnread: Boolean get() = paging.items.any { !it.isRead }
}

sealed interface NotificationsViewAction {
    data object OnRefresh : NotificationsViewAction
    data object OnLoadMore : NotificationsViewAction
    data object OnRetry : NotificationsViewAction
    data class OnFilterSelected(val filter: NotificationFilter) : NotificationsViewAction
    data class OnNotificationClicked(val id: String) : NotificationsViewAction
    data object OnMarkAllReadClicked : NotificationsViewAction
    data object OnBackClicked : NotificationsViewAction
}

sealed interface NotificationsEvent {
    data class ShowMessage(val text: UiText) : NotificationsEvent
    data object NavigateBack : NotificationsEvent
}
