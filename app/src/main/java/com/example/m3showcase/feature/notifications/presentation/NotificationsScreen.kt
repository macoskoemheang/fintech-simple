package com.example.m3showcase.feature.notifications.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.m3showcase.core.designsystem.bankTopAppBarColors
import com.example.m3showcase.core.designsystem.component.EmptyState
import com.example.m3showcase.core.designsystem.component.PagedLazyColumn
import com.example.m3showcase.feature.notifications.domain.model.AppNotification
import com.example.m3showcase.feature.notifications.domain.model.NotificationCategory
import com.example.m3showcase.feature.notifications.domain.model.NotificationFilter
import org.koin.androidx.compose.koinViewModel
import java.util.Calendar
import androidx.compose.ui.res.stringResource
import com.example.m3showcase.R
import com.example.m3showcase.core.ui.asString
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.annotation.StringRes
import com.example.m3showcase.core.ui.relativeDayText

@Composable
fun NotificationsRoute(onBack: () -> Unit, viewModel: NotificationsViewModel = koinViewModel()) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is NotificationsEvent.ShowMessage -> snackbarHostState.showSnackbar(event.text.asString(context))
                NotificationsEvent.NavigateBack -> onBack()
            }
        }
    }
    NotificationsScreen(state, snackbarHostState, viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NotificationsScreen(
    state: NotificationsViewState,
    snackbarHostState: SnackbarHostState,
    onAction: (NotificationsViewAction) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    // One list state per filter: switching filters starts at the top instead of a stale offset.
    val listState = rememberSaveable(state.filter, saver = LazyListState.Saver) { LazyListState() }
    Scaffold(
        containerColor = Color.Transparent, // AppBackground shows through.
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                colors = bankTopAppBarColors(),
                scrollBehavior = scrollBehavior,
                title = { Text(stringResource(R.string.notifications_title)) },
                navigationIcon = {
                    IconButton(onClick = { onAction(NotificationsViewAction.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    if (state.isMarkingAllRead) {
                        LoadingIndicator(Modifier.padding(end = 12.dp).size(32.dp))
                    } else {
                        IconButton(
                            onClick = { onAction(NotificationsViewAction.OnMarkAllReadClicked) },
                            enabled = state.hasUnread,
                        ) { Icon(Icons.Rounded.DoneAll, contentDescription = stringResource(R.string.notifications_mark_all_read)) }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState, Modifier.navigationBarsPadding()) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            FilterRow(state.filter) { onAction(NotificationsViewAction.OnFilterSelected(it)) }
            PagedLazyColumn(
                state = state.paging,
                onRefresh = { onAction(NotificationsViewAction.OnRefresh) },
                onLoadMore = { onAction(NotificationsViewAction.OnLoadMore) },
                onRetry = { onAction(NotificationsViewAction.OnRetry) },
                listState = listState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                endLabel = { pluralStringResource(R.plurals.notifications_end, it, it) },
                emptyContent = { EmptyFor(state.filter) },
                header = {
                    state.paging.totalCount?.let { total ->
                        item(key = "count", contentType = "count") {
                            Text(
                                stringResource(R.string.notifications_showing, state.paging.items.size, total),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                            )
                        }
                    }
                },
            ) { items ->
                groupByDay(items).forEach { (section, rows) ->
                    item(key = "section-$section", contentType = "section") {
                        Text(
                            stringResource(section.labelRes),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.animateItem().padding(start = 4.dp, top = 12.dp, bottom = 4.dp),
                        )
                    }
                    items(rows, key = { it.id }, contentType = { "notification" }) { notification ->
                        NotificationRow(
                            notification,
                            onClick = { onAction(NotificationsViewAction.OnNotificationClicked(notification.id)) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(selected: NotificationFilter, onSelect: (NotificationFilter) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
    ) {
        items(NotificationFilter.entries, key = { it.name }) { filter ->
            val isSelected = filter == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(filter) },
                label = { Text(stringResource(filter.labelRes())) },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Rounded.Check, null, Modifier.size(FilterChipDefaults.IconSize)) }
                } else {
                    null
                },
            )
        }
    }
}

/** Unread rows are tinted and bold with a dot; tapping one marks it read and expands its text. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun NotificationRow(notification: AppNotification, onClick: () -> Unit, modifier: Modifier = Modifier) {
    var expanded by rememberSaveable(notification.id) { mutableStateOf(false) }
    val style = notification.category.style()
    val unreadAlpha by animateFloatAsState(if (notification.isRead) 0f else 1f, label = "unread")

    Surface(
        onClick = {
            expanded = !expanded
            onClick()
        },
        shape = MaterialTheme.shapes.large,
        // Unread rows carry a primary tint that fades out once read.
        color = lerp(
            MaterialTheme.colorScheme.surfaceContainerLowest,
            MaterialTheme.colorScheme.primaryContainer,
            0.45f * unreadAlpha,
        ).copy(alpha = 0.94f),
        modifier = modifier.fillMaxWidth(),
    ) {
            Row(Modifier.padding(16.dp).animateContentSize(), verticalAlignment = Alignment.Top) {
                Surface(
                    shape = style.shape.toShape(),
                    color = style.container(),
                    contentColor = style.content(),
                    modifier = Modifier.size(44.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) { Icon(style.icon, contentDescription = null, Modifier.size(22.dp)) }
                }
                Column(Modifier.weight(1f).padding(start = 14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            notification.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (unreadAlpha > 0f) {
                            Surface(
                                shape = MaterialShapes.Circle.toShape(),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = unreadAlpha),
                                modifier = Modifier.padding(start = 8.dp).size(8.dp),
                            ) {}
                        }
                    }
                    Text(
                        notification.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (expanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                    Text(
                        "${stringResource(notification.category.labelRes())} · ${relativeDayText(notification.timestamp)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
    }
}

@Composable
private fun EmptyFor(filter: NotificationFilter) = when (filter) {
    NotificationFilter.Unread -> EmptyState(
        stringResource(R.string.notifications_empty_unread_title),
        stringResource(R.string.notifications_empty_unread_message),
        icon = Icons.Rounded.DoneAll,
    )
    NotificationFilter.All -> EmptyState(
        stringResource(R.string.notifications_empty_title),
        stringResource(R.string.notifications_empty_message),
        icon = Icons.Outlined.NotificationsNone,
    )
    else -> EmptyState(
        stringResource(R.string.notifications_empty_filter_title, stringResource(filter.labelRes())),
        stringResource(R.string.notifications_empty_filter_message),
        icon = Icons.Outlined.NotificationsNone,
    )
}

@StringRes
private fun NotificationFilter.labelRes() = when (this) {
    NotificationFilter.All -> R.string.filter_all
    NotificationFilter.Unread -> R.string.notifications_filter_unread
    NotificationFilter.Transactions -> R.string.notifications_filter_transactions
    NotificationFilter.Security -> R.string.notifications_filter_security
    NotificationFilter.Promotions -> R.string.notifications_filter_offers
}

@StringRes
private fun NotificationCategory.labelRes() = when (this) {
    NotificationCategory.Transaction -> R.string.notifications_category_transaction
    NotificationCategory.Security -> R.string.notifications_filter_security
    NotificationCategory.Promotion -> R.string.notifications_category_offer
    NotificationCategory.System -> R.string.notifications_category_service
}

private enum class DaySection(@StringRes val labelRes: Int) {
    Today(R.string.section_today),
    Yesterday(R.string.section_yesterday),
    Earlier(R.string.section_earlier),
}

/** Icon, Expressive shape and colors per category, so the inbox can be scanned by shape and color. */
private class CategoryStyle(
    val icon: ImageVector,
    val shape: androidx.graphics.shapes.RoundedPolygon,
    val container: @Composable () -> Color,
    val content: @Composable () -> Color,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun NotificationCategory.style(): CategoryStyle = when (this) {
    NotificationCategory.Transaction -> CategoryStyle(
        Icons.Rounded.SwapHoriz, MaterialShapes.Cookie9Sided,
        { MaterialTheme.colorScheme.primaryContainer }, { MaterialTheme.colorScheme.onPrimaryContainer },
    )
    NotificationCategory.Security -> CategoryStyle(
        Icons.Rounded.Shield, MaterialShapes.Pentagon,
        { MaterialTheme.colorScheme.errorContainer }, { MaterialTheme.colorScheme.onErrorContainer },
    )
    NotificationCategory.Promotion -> CategoryStyle(
        Icons.Rounded.Campaign, MaterialShapes.Sunny,
        { MaterialTheme.colorScheme.tertiaryContainer }, { MaterialTheme.colorScheme.onTertiaryContainer },
    )
    NotificationCategory.System -> CategoryStyle(
        Icons.Rounded.Info, MaterialShapes.Clover4Leaf,
        { MaterialTheme.colorScheme.secondaryContainer }, { MaterialTheme.colorScheme.onSecondaryContainer },
    )
}

/** Splits newest-first rows into Today / Yesterday / Earlier, keeping order. */
private fun groupByDay(items: List<AppNotification>, now: Long = System.currentTimeMillis()): List<Pair<DaySection, List<AppNotification>>> {
    fun dayStart(millis: Long) = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val today = dayStart(now)
    val yesterday = today - 24 * 60 * 60 * 1000L
    return items.groupBy {
        when {
            it.timestamp >= today -> DaySection.Today
            it.timestamp >= yesterday -> DaySection.Yesterday
            else -> DaySection.Earlier
        }
    }.toList()
}
