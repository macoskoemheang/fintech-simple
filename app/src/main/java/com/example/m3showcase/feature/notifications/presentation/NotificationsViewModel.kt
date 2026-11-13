package com.example.m3showcase.feature.notifications.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m3showcase.core.paging.Paginator
import com.example.m3showcase.core.ui.toUserMessage
import com.example.m3showcase.feature.notifications.domain.model.AppNotification
import com.example.m3showcase.feature.notifications.domain.model.NotificationFilter
import com.example.m3showcase.feature.notifications.domain.usecase.GetNotificationsPageUseCase
import com.example.m3showcase.feature.notifications.domain.usecase.MarkAllNotificationsReadUseCase
import com.example.m3showcase.feature.notifications.domain.usecase.MarkNotificationReadUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.m3showcase.core.ui.UiText
import com.example.m3showcase.R

class NotificationsViewModel(
    private val getNotificationsPage: GetNotificationsPageUseCase,
    private val markNotificationRead: MarkNotificationReadUseCase,
    private val markAllNotificationsRead: MarkAllNotificationsReadUseCase,
) : ViewModel() {

    private val filter = MutableStateFlow(NotificationFilter.All)
    private val isMarkingAllRead = MutableStateFlow(false)

    private val _events = Channel<NotificationsEvent>(Channel.BUFFERED)
    val events: Flow<NotificationsEvent> = _events.receiveAsFlow()

    /** Reads the current filter on every page, so reset() after a filter change is all it takes. */
    private val paginator = Paginator(viewModelScope, pageSize = PAGE_SIZE, keyOf = AppNotification::id) { request ->
        getNotificationsPage(filter.value, request)
    }

    val viewState: StateFlow<NotificationsViewState> =
        combine(filter, paginator.state, isMarkingAllRead) { f, paging, marking ->
            NotificationsViewState(filter = f, paging = paging, isMarkingAllRead = marking)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotificationsViewState())

    init {
        paginator.start()
        viewModelScope.launch {
            paginator.refreshErrors.collect { _events.send(NotificationsEvent.ShowMessage(it.toUserMessage())) }
        }
    }

    fun onAction(action: NotificationsViewAction) {
        when (action) {
            NotificationsViewAction.OnRefresh -> paginator.refresh()
            NotificationsViewAction.OnLoadMore -> paginator.loadNext()
            NotificationsViewAction.OnRetry -> paginator.retry()
            is NotificationsViewAction.OnFilterSelected -> if (action.filter != filter.value) {
                filter.value = action.filter
                paginator.reset()
            }
            is NotificationsViewAction.OnNotificationClicked -> markRead(action.id)
            NotificationsViewAction.OnMarkAllReadClicked -> markAllRead()
            NotificationsViewAction.OnBackClicked -> _events.trySend(NotificationsEvent.NavigateBack)
        }
    }

    /**
     * Optimistic: the row turns "read" at once, then the server is told.
     * On failure the row goes back to unread and the user is told why.
     * Under the Unread filter the row stays until the next refresh, so the list doesn't jump.
     */
    private fun markRead(id: String) {
        if (paginator.state.value.items.firstOrNull { it.id == id }?.isRead != false) return
        paginator.updateItems { items -> items.map { if (it.id == id) it.copy(isRead = true) else it } }
        viewModelScope.launch {
            markNotificationRead(id).onFailure { error ->
                paginator.updateItems { items -> items.map { if (it.id == id) it.copy(isRead = false) else it } }
                _events.send(NotificationsEvent.ShowMessage(error.toUserMessage()))
            }
        }
    }

    private fun markAllRead() {
        if (isMarkingAllRead.value) return
        val unreadIds = paginator.state.value.items.filterNot { it.isRead }.map { it.id }.toSet()
        isMarkingAllRead.value = true
        paginator.updateItems { items -> items.map { it.copy(isRead = true) } }
        viewModelScope.launch {
            markAllNotificationsRead()
                .onSuccess { _events.send(NotificationsEvent.ShowMessage(UiText.res(R.string.notifications_all_marked_read))) }
                .onFailure { error ->
                    // Roll back only what this call changed; items paged in meanwhile keep their state.
                    paginator.updateItems { items -> items.map { if (it.id in unreadIds) it.copy(isRead = false) else it } }
                    _events.send(NotificationsEvent.ShowMessage(error.toUserMessage()))
                }
            isMarkingAllRead.value = false
        }
    }

    companion object {
        const val PAGE_SIZE = 15
    }
}
