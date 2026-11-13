package com.example.m3showcase.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.m3showcase.core.domain.model.BankError
import com.example.m3showcase.core.paging.PagingState
import com.example.m3showcase.core.ui.toUserMessage
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import com.example.m3showcase.R
import androidx.compose.ui.res.pluralStringResource
import com.example.m3showcase.core.ui.asString

/**
 * Base for every paged list: pull-to-refresh, infinite scroll and all load states from one
 * [PagingState]. Screens only describe their rows.
 *
 * - First load: full-screen loader, or error with retry.
 * - Empty: [emptyContent], still pull-to-refreshable.
 * - Loaded: rows from [content], then a footer that shows "loading more", an inline retry,
 *   or the end-of-list note with the item count.
 * - When the last visible row is within [prefetchDistance] of the end, [onLoadMore] fires.
 *   The Paginator ignores calls while busy, so this can fire freely.
 * - A "back to top" button appears after scrolling past [scrollToTopAfter] rows.
 *
 * @param content adds the rows; use it for section headers too. The simpler overload takes one row per item.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> PagedLazyColumn(
    state: PagingState<T>,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(vertical = 8.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    prefetchDistance: Int = 5,
    scrollToTopAfter: Int = 12,
    /** Footer at the end of the list, given the item count. */
    endLabel: @Composable (count: Int) -> String = { pluralStringResource(R.plurals.paging_end_items, it, it) },
    emptyContent: @Composable () -> Unit = { DefaultEmpty() },
    header: (LazyListScope.() -> Unit)? = null,
    content: LazyListScope.(items: List<T>) -> Unit,
) {
    when {
        state.isInitialLoading -> LoadingState(modifier)
        state.initialError != null -> ErrorState(
            message = state.initialError.toUserMessage().asString(),
            onRetry = onRetry,
            isOffline = state.initialError is BankError.NoInternet,
            modifier = modifier,
        )
        else -> PullToRefreshBox(isRefreshing = state.isRefreshing, onRefresh = onRefresh, modifier = modifier.fillMaxSize()) {
            if (state.isEmpty) {
                // Scrollable so the pull gesture still works on an empty screen.
                Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) { emptyContent() }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = contentPadding,
                    verticalArrangement = verticalArrangement,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    header?.invoke(this)
                    content(state.items)
                    item(key = "paging-footer", contentType = "paging-footer") { PagingFooter(state, endLabel, onRetry) }
                }
                LoadMoreTrigger(listState, prefetchDistance, state.canLoadMore, onLoadMore)
                ScrollToTopButton(listState, scrollToTopAfter, Modifier.align(Alignment.BottomEnd))
            }
        }
    }
}

/** Convenience overload: one row per item, keyed by [key]. */
@Composable
fun <T> PagedLazyColumn(
    state: PagingState<T>,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    key: (T) -> Any,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(vertical = 8.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    endLabel: @Composable (count: Int) -> String = { pluralStringResource(R.plurals.paging_end_items, it, it) },
    emptyContent: @Composable () -> Unit = { DefaultEmpty() },
    itemContent: @Composable LazyItemScope.(T) -> Unit,
) = PagedLazyColumn(
    state = state,
    onRefresh = onRefresh,
    onLoadMore = onLoadMore,
    onRetry = onRetry,
    modifier = modifier,
    listState = listState,
    contentPadding = contentPadding,
    verticalArrangement = verticalArrangement,
    endLabel = endLabel,
    emptyContent = emptyContent,
) { items -> items(items, key = key) { itemContent(it) } }

/**
 * Watches scroll position, not composition, so it costs nothing per frame:
 * snapshotFlow only emits when "near the end" flips.
 */
@Composable
private fun LoadMoreTrigger(listState: LazyListState, prefetchDistance: Int, canLoadMore: Boolean, onLoadMore: () -> Unit) {
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)
    val nearEnd by remember(listState, prefetchDistance) {
        derivedStateOf {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            lastVisible >= info.totalItemsCount - 1 - prefetchDistance
        }
    }
    // Keyed on canLoadMore so a short first page (that doesn't fill the screen) keeps loading
    // until the screen is full or the end is reached.
    LaunchedEffect(listState, canLoadMore) {
        if (!canLoadMore) return@LaunchedEffect
        snapshotFlow { nearEnd }.distinctUntilChanged().filter { it }.collect { currentOnLoadMore() }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun <T> PagingFooter(state: PagingState<T>, endLabel: @Composable (Int) -> String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 16.dp), contentAlignment = Alignment.Center) {
        when {
            state.isAppending -> LoadingIndicator(Modifier.size(40.dp))
            state.appendError != null -> Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(start = 16.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.paging_load_more_failed, state.appendError.toUserMessage().asString()),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f).padding(vertical = 12.dp),
                    )
                    TextButton(onClick = onRetry) {
                        Icon(Icons.Rounded.Refresh, null, Modifier.size(18.dp))
                        Text(stringResource(R.string.action_retry), Modifier.padding(start = 6.dp))
                    }
                }
            }
            state.endReached -> Text(
                endLabel(state.items.size),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ScrollToTopButton(listState: LazyListState, threshold: Int, modifier: Modifier) {
    val scope = rememberCoroutineScope()
    val visible by remember(listState) { derivedStateOf { listState.firstVisibleItemIndex > threshold } }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier.navigationBarsPadding().padding(16.dp),
    ) {
        SmallFloatingActionButton(onClick = { scope.launch { listState.animateScrollToItem(0) } }) {
            Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = stringResource(R.string.paging_back_to_top))
        }
    }
}

@Composable
private fun DefaultEmpty() =
    EmptyState(stringResource(R.string.paging_empty_title), stringResource(R.string.paging_empty_message))
