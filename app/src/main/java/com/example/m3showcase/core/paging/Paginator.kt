package com.example.m3showcase.core.paging

import androidx.compose.runtime.Immutable
import com.example.m3showcase.core.domain.model.Page
import com.example.m3showcase.core.domain.model.PageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Everything a paged list screen needs to render. Pure data, so it drops straight into a ViewState.
 *
 * Load phases:
 * - initial: first page with nothing on screen yet (full-screen loader / error / empty).
 * - refresh: pull-to-refresh while items stay visible.
 * - append: the next page, shown as a footer under the list.
 */
@Immutable
data class PagingState<T>(
    val items: List<T> = emptyList(),
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isAppending: Boolean = false,
    /** First-page failure with nothing to show. Full-screen error. */
    val initialError: Throwable? = null,
    /** Next-page failure. The list stays, the footer offers a retry. */
    val appendError: Throwable? = null,
    val endReached: Boolean = false,
    val totalCount: Int? = null,
) {
    val isEmpty: Boolean get() = !isInitialLoading && initialError == null && items.isEmpty()

    /** True when scrolling near the end should request another page. */
    val canLoadMore: Boolean
        get() = !isInitialLoading && !isRefreshing && !isAppending && appendError == null && initialError == null && !endReached
}

/**
 * Reusable pagination engine. A ViewModel owns one, forwards its list actions to it and
 * maps [state] into its ViewState. It is plain Kotlin (no Android, no Compose runtime calls),
 * so it is unit-tested directly.
 *
 * Guarantees:
 * - At most one load runs at a time. [loadNext] while loading is ignored, so a fast fling
 *   can't request the same page twice.
 * - [refresh] and [reset] cancel an in-flight page, so a stale page can never land on top of
 *   fresh data (e.g. after the filter changes).
 * - Items are de-duplicated by [keyOf], so a row that shifted between pages on the server
 *   (new items arrived at the top) is not shown twice.
 * - A failed page is not retried automatically on scroll; the user taps retry. That avoids
 *   hammering a failing server while the footer is visible.
 *
 * @param loadPage the use case call. Must return failures as Result, not throw.
 */
class Paginator<T>(
    private val scope: CoroutineScope,
    private val pageSize: Int = DEFAULT_PAGE_SIZE,
    private val keyOf: (T) -> Any,
    private val loadPage: suspend (PageRequest) -> Result<Page<T>>,
) {
    private val _state = MutableStateFlow(PagingState<T>())
    val state: StateFlow<PagingState<T>> = _state.asStateFlow()

    /** Refresh failures while items are on screen. Collect once and show a snackbar. */
    private val _refreshErrors = Channel<Throwable>(Channel.BUFFERED)
    val refreshErrors: Flow<Throwable> = _refreshErrors.receiveAsFlow()

    private var nextCursor: String? = null
    private var job: Job? = null
    private var started = false

    /** Loads the first page if nothing has been loaded yet. Safe to call repeatedly. */
    fun start() {
        if (started) return
        started = true
        loadFirstPage(asRefresh = false)
    }

    /** Pull-to-refresh: reloads page one but keeps current items visible until it arrives. */
    fun refresh() {
        if (_state.value.items.isEmpty()) {
            reset()
        } else {
            loadFirstPage(asRefresh = true)
        }
    }

    /** Clears everything and shows the full-screen loader. Use when the query (filter) changes. */
    fun reset() {
        started = true
        job?.cancel()
        nextCursor = null
        _state.value = PagingState()
        loadFirstPage(asRefresh = false)
    }

    /** Asks for the next page. No-op while busy, after an append error, or at the end. */
    fun loadNext() {
        if (!_state.value.canLoadMore || job?.isActive == true) return
        val cursor = nextCursor ?: return
        _state.update { it.copy(isAppending = true, appendError = null) }
        job = scope.launch {
            loadPage(PageRequest(cursor, pageSize))
                .onSuccess { page ->
                    nextCursor = page.nextCursor
                    _state.update {
                        it.copy(
                            items = (it.items + page.items).distinctBy(keyOf),
                            isAppending = false,
                            endReached = page.isLast,
                            totalCount = page.totalCount ?: it.totalCount,
                        )
                    }
                }
                .onFailure { error -> _state.update { it.copy(isAppending = false, appendError = error) } }
        }
    }

    /** Retries whichever load failed last. */
    fun retry() {
        val s = _state.value
        when {
            s.initialError != null -> reset()
            s.appendError != null -> {
                _state.update { it.copy(appendError = null) }
                loadNext()
            }
        }
    }

    /**
     * Local edit of loaded items (optimistic "mark as read", delete, …) without reloading.
     * Returns the previous list so the caller can roll back if the server call fails.
     */
    fun updateItems(transform: (List<T>) -> List<T>): List<T> {
        val before = _state.value.items
        _state.update { it.copy(items = transform(it.items)) }
        return before
    }

    private fun loadFirstPage(asRefresh: Boolean) {
        job?.cancel()
        _state.update {
            // A refresh cancels any page in flight, so clear its flag too.
            if (asRefresh) it.copy(isRefreshing = true, isAppending = false, appendError = null)
            else it.copy(isInitialLoading = true, initialError = null)
        }
        job = scope.launch {
            loadPage(PageRequest(cursor = null, size = pageSize))
                .onSuccess { page ->
                    nextCursor = page.nextCursor
                    _state.value = PagingState(
                        items = page.items.distinctBy(keyOf),
                        isInitialLoading = false,
                        endReached = page.isLast,
                        totalCount = page.totalCount,
                    )
                }
                .onFailure { error ->
                    if (asRefresh) {
                        // Keep what the user is reading; just tell them the refresh failed.
                        _state.update { it.copy(isRefreshing = false) }
                        _refreshErrors.trySend(error)
                    } else {
                        _state.update { it.copy(isInitialLoading = false, initialError = error) }
                    }
                }
        }
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }
}
