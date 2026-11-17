package com.example.m3showcase.feature.booking.presentation.hub

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Hub
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Badge
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.m3showcase.R
import com.example.m3showcase.core.common.MoneyFormatter
import com.example.m3showcase.core.designsystem.component.BankScreenScaffold
import com.example.m3showcase.core.designsystem.component.EmptyState
import com.example.m3showcase.core.designsystem.component.LoadableContent
import com.example.m3showcase.core.designsystem.component.StatusPill
import com.example.m3showcase.core.ui.Loadable
import com.example.m3showcase.core.ui.asString
import com.example.m3showcase.core.ui.dateText
import com.example.m3showcase.feature.booking.domain.model.Booking
import com.example.m3showcase.feature.booking.domain.model.BookingStatus
import com.example.m3showcase.feature.booking.domain.model.CancellationPolicy
import com.example.m3showcase.feature.booking.domain.model.Listing
import com.example.m3showcase.feature.booking.domain.model.ListingCategory
import com.example.m3showcase.feature.booking.domain.model.McpConnectionState
import com.example.m3showcase.feature.booking.domain.model.PriceUnit
import com.example.m3showcase.feature.booking.presentation.BookingStatusPill
import com.example.m3showcase.feature.booking.presentation.CategoryBadge
import com.example.m3showcase.feature.booking.presentation.formatRating
import com.example.m3showcase.feature.booking.presentation.icon
import com.example.m3showcase.feature.booking.presentation.labelRes
import com.example.m3showcase.feature.booking.presentation.toBookingMessage
import org.koin.androidx.compose.koinViewModel

@Composable
fun BookingHubRoute(
    onBack: () -> Unit,
    onOpenListing: (String) -> Unit,
    onOpenBooking: (String) -> Unit,
    onOpenInspector: () -> Unit,
    viewModel: BookingHubViewModel = koinViewModel(),
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is BookingHubEvent.OpenListing -> onOpenListing(event.id)
                is BookingHubEvent.OpenBooking -> onOpenBooking(event.id)
                BookingHubEvent.OpenInspector -> onOpenInspector()
                BookingHubEvent.NavigateBack -> onBack()
            }
        }
    }
    BookingHubScreen(state, viewModel::onAction)
}

@Composable
fun BookingHubScreen(state: BookingHubViewState, onAction: (BookingHubViewAction) -> Unit) {
    BankScreenScaffold(
        title = stringResource(R.string.booking_title),
        onBack = { onAction(BookingHubViewAction.OnBackClicked) },
        actions = {
            IconButton(onClick = { onAction(BookingHubViewAction.OnInspectorClicked) }) {
                Icon(Icons.Rounded.Terminal, contentDescription = stringResource(R.string.booking_inspector_title))
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            McpConnectionCard(
                state = state.connection,
                onClick = { onAction(BookingHubViewAction.OnInspectorClicked) },
                onReconnect = { onAction(BookingHubViewAction.OnReconnect) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
            val upcoming = (state.bookings as? Loadable.Success)?.data?.upcoming?.size ?: 0
            TabToggle(state.tab, upcoming) { onAction(BookingHubViewAction.OnTabSelected(it)) }
            AnimatedContent(state.tab, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "booking-tab") { tab ->
                when (tab) {
                    BookingTab.Explore -> ExploreContent(state, onAction)
                    BookingTab.Trips -> TripsContent(state, onAction)
                }
            }
        }
    }
}

// region Connection

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun McpConnectionCard(state: McpConnectionState, onClick: () -> Unit, onReconnect: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        color = scheme.surfaceContainerLowest.copy(alpha = 0.94f),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                if (state is McpConnectionState.Connecting) {
                    LoadingIndicator(Modifier.size(44.dp))
                } else {
                    val failed = state is McpConnectionState.Failed
                    Surface(
                        shape = MaterialShapes.Cookie12Sided.toShape(),
                        color = if (failed) scheme.errorContainer else scheme.tertiaryContainer,
                        contentColor = if (failed) scheme.onErrorContainer else scheme.onTertiaryContainer,
                        modifier = Modifier.size(44.dp),
                    ) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Hub, null, Modifier.size(22.dp)) } }
                }
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    (state as? McpConnectionState.Connected)?.server?.title ?: stringResource(R.string.booking_mcp_server),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    when (state) {
                        McpConnectionState.Idle -> stringResource(R.string.booking_mcp_idle)
                        McpConnectionState.Connecting -> stringResource(R.string.booking_mcp_connecting)
                        is McpConnectionState.Connected -> stringResource(R.string.booking_mcp_summary, state.server.version, state.tools.size, state.server.transport)
                        is McpConnectionState.Failed -> state.error.toBookingMessage().asString()
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            when (state) {
                is McpConnectionState.Connected ->
                    StatusPill(stringResource(R.string.booking_mcp_connected), scheme.primaryContainer, scheme.onPrimaryContainer)
                is McpConnectionState.Failed, McpConnectionState.Idle ->
                    FilledTonalButton(onClick = onReconnect, shapes = ButtonDefaults.shapes()) {
                        Text(stringResource(R.string.booking_mcp_reconnect))
                    }
                McpConnectionState.Connecting -> Unit
            }
        }
    }
}

// endregion

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TabToggle(selected: BookingTab, upcomingCount: Int, onSelect: (BookingTab) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        BookingTab.entries.forEachIndexed { index, tab ->
            ToggleButton(
                checked = selected == tab,
                onCheckedChange = { onSelect(tab) },
                modifier = Modifier.weight(1f).semantics { role = Role.RadioButton },
                shapes = if (index == 0) ButtonGroupDefaults.connectedLeadingButtonShapes() else ButtonGroupDefaults.connectedTrailingButtonShapes(),
            ) {
                Icon(if (tab == BookingTab.Explore) Icons.Outlined.TravelExplore else Icons.Outlined.Luggage, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(if (tab == BookingTab.Explore) R.string.booking_tab_explore else R.string.booking_tab_trips))
                if (tab == BookingTab.Trips && upcomingCount > 0) {
                    Spacer(Modifier.width(6.dp))
                    Badge { Text(upcomingCount.toString()) }
                }
            }
        }
    }
}

// region Explore

@Composable
private fun ExploreContent(state: BookingHubViewState, onAction: (BookingHubViewAction) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = state.query,
            onValueChange = { onAction(BookingHubViewAction.OnQueryChanged(it)) },
            placeholder = { Text(stringResource(R.string.booking_search)) },
            leadingIcon = { Icon(Icons.Rounded.Search, null) },
            trailingIcon = if (state.query.isNotEmpty()) {
                {
                    IconButton(onClick = { onAction(BookingHubViewAction.OnQueryChanged("")) }) {
                        Icon(Icons.Rounded.Close, stringResource(R.string.locator_clear_search))
                    }
                }
            } else null,
            singleLine = true,
            shape = RoundedCornerShape(50),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )
        CategoryChips(state.category) { onAction(BookingHubViewAction.OnCategorySelected(it)) }
        LoadableContent(state.listings, onRetry = { onAction(BookingHubViewAction.OnRetry) }) { listings ->
            if (listings.isEmpty()) {
                EmptyState(stringResource(R.string.booking_empty_title), stringResource(R.string.booking_empty_message), icon = Icons.Outlined.TravelExplore)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(listings, key = { it.id }) { listing ->
                        ListingCard(listing, { onAction(BookingHubViewAction.OnListingClicked(listing.id)) }, Modifier.animateItem())
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChips(selected: ListingCategory?, onSelect: (ListingCategory?) -> Unit) {
    val options = listOf<ListingCategory?>(null) + ListingCategory.entries
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
    ) {
        items(options, key = { it?.name ?: "all" }) { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(stringResource(option?.labelRes ?: R.string.filter_all)) },
                leadingIcon = option?.let { { Icon(it.icon, null, Modifier.size(18.dp)) } },
            )
        }
    }
}

@Composable
private fun ListingCard(listing: Listing, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.94f),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            CategoryBadge(listing.category)
            Column(Modifier.weight(1f).padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(listing.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Star, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.tertiary)
                    Text(
                        " ${formatRating(listing.rating)} · ${listing.city}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                if (listing.cancellation == CancellationPolicy.Free48h) {
                    Text(stringResource(R.string.booking_free_cancel), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(MoneyFormatter.format(listing.price), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    stringResource(if (listing.unit == PriceUnit.Night) R.string.booking_per_night else R.string.booking_per_person),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// endregion

// region Trips

@Composable
private fun TripsContent(state: BookingHubViewState, onAction: (BookingHubViewAction) -> Unit) {
    LoadableContent(state.bookings, onRetry = { onAction(BookingHubViewAction.OnRetry) }) { groups ->
        if (groups.upcoming.isEmpty() && groups.past.isEmpty()) {
            EmptyState(stringResource(R.string.booking_trips_empty_title), stringResource(R.string.booking_trips_empty_message), icon = Icons.Outlined.Luggage)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (groups.upcoming.isNotEmpty()) {
                    item(key = "h-upcoming", contentType = "header") { SectionHeader(stringResource(R.string.booking_upcoming)) }
                    items(groups.upcoming, key = { it.id }, contentType = { "booking" }) { booking ->
                        BookingRow(booking, { onAction(BookingHubViewAction.OnBookingClicked(booking.id)) }, Modifier.animateItem())
                    }
                }
                if (groups.past.isNotEmpty()) {
                    item(key = "h-past", contentType = "header") { SectionHeader(stringResource(R.string.booking_past)) }
                    items(groups.past, key = { it.id }, contentType = { "booking" }) { booking ->
                        BookingRow(booking, { onAction(BookingHubViewAction.OnBookingClicked(booking.id)) }, Modifier.animateItem())
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp),
    )
}

@Composable
private fun BookingRow(booking: Booking, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val dimmed = booking.status != BookingStatus.Confirmed
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = if (dimmed) 0.7f else 0.94f),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            CategoryBadge(booking.listing.category, size = 44.dp)
            Column(Modifier.weight(1f).padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(booking.listing.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${dateText(booking.checkIn)} · ${booking.confirmationCode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(MoneyFormatter.format(booking.total), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                BookingStatusPill(booking.status)
            }
        }
    }
}

// endregion
