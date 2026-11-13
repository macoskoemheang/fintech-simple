package com.example.m3showcase.feature.locator.presentation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.LocalAtm
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.m3showcase.R
import com.example.m3showcase.core.designsystem.BankTheme
import com.example.m3showcase.core.designsystem.component.BankScreenScaffold
import com.example.m3showcase.core.designsystem.component.EmptyState
import com.example.m3showcase.core.designsystem.component.LoadableContent
import com.example.m3showcase.core.designsystem.component.StatusPill
import com.example.m3showcase.core.ui.timeOfDayText
import com.example.m3showcase.feature.locator.domain.model.BankLocation
import com.example.m3showcase.feature.locator.domain.model.LocationType
import com.example.m3showcase.feature.locator.domain.model.NearbyLocation
import com.example.m3showcase.feature.locator.domain.model.TimeRange
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@Composable
fun LocatorRoute(onBack: () -> Unit, viewModel: LocatorViewModel = koinViewModel()) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is LocatorEvent.OpenDirections -> context.openDirections(event.location)
                is LocatorEvent.Dial -> context.startSafely(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${event.phone.filter(Char::isDigit)}")))
                LocatorEvent.NavigateBack -> onBack()
            }
        }
    }
    LocatorScreen(state, viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocatorScreen(state: LocatorViewState, onAction: (LocatorViewAction) -> Unit) {
    BankScreenScaffold(title = stringResource(R.string.locator_title), onBack = { onAction(LocatorViewAction.OnBackClicked) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            SearchField(state.query) { onAction(LocatorViewAction.OnQueryChanged(it)) }
            Filters(state, onAction)
            LoadableContent(state.content, onRetry = { onAction(LocatorViewAction.OnRetry) }) { locations ->
                if (locations.isEmpty()) {
                    EmptyState(stringResource(R.string.locator_empty_title), stringResource(R.string.locator_empty_message), icon = Icons.Outlined.LocationOff)
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(locations, key = { it.location.id }) { item ->
                            LocationRow(item, { onAction(LocatorViewAction.OnLocationClicked(item.location.id)) }, Modifier.animateItem())
                        }
                    }
                }
            }
        }
    }
    state.selected?.let { LocationSheet(it, onAction) }
}

@Composable
private fun SearchField(query: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onChange,
        placeholder = { Text(stringResource(R.string.locator_search)) },
        leadingIcon = { Icon(Icons.Rounded.Search, null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onChange("") }) { Icon(Icons.Rounded.Close, stringResource(R.string.locator_clear_search)) }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(50),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
    )
}

@Composable
private fun Filters(state: LocatorViewState, onAction: (LocatorViewAction) -> Unit) {
    val types = listOf<LocationType?>(null) + LocationType.entries
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item(key = "open") {
            FilterChip(
                selected = state.openNowOnly,
                onClick = { onAction(LocatorViewAction.OnOpenNowToggled(!state.openNowOnly)) },
                label = { Text(stringResource(R.string.locator_open_now)) },
                leadingIcon = { Icon(Icons.Rounded.Schedule, null, Modifier.size(18.dp)) },
            )
        }
        items(types, key = { it?.name ?: "all" }) { type ->
            FilterChip(
                selected = state.type == type,
                onClick = { onAction(LocatorViewAction.OnTypeSelected(type)) },
                label = { Text(stringResource(type?.labelRes ?: R.string.filter_all)) },
            )
        }
    }
}

@Composable
private fun LocationRow(item: NearbyLocation, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val location = item.location
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.94f),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            TypeIcon(location.type)
            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text(location.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(location.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Box(Modifier.padding(top = 6.dp)) { OpenStatus(item) }
            }
            Text(distanceText(item.distanceKm), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TypeIcon(type: LocationType, size: Int = 44) {
    Surface(
        shape = type.shape(),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.size(size.dp),
    ) { Box(contentAlignment = Alignment.Center) { Icon(type.icon, null, Modifier.size((size / 2).dp)) } }
}

/** "Open · until 4:00 PM", "Open 24 hours" or "Closed". */
@Composable
private fun OpenStatus(item: NearbyLocation) {
    val credit = BankTheme.colors.credit
    when {
        item.location.hours.is24Hours -> StatusPill(stringResource(R.string.locator_open_24h), credit.copy(alpha = 0.16f), credit)
        item.isOpenNow -> StatusPill(
            stringResource(R.string.locator_open_until, timeOfDayText(item.todayHours!!.close)),
            credit.copy(alpha = 0.16f),
            credit,
        )
        else -> StatusPill(stringResource(R.string.locator_closed), MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationSheet(item: NearbyLocation, onAction: (LocatorViewAction) -> Unit) {
    val location = item.location
    ModalBottomSheet(onDismissRequest = { onAction(LocatorViewAction.OnSheetDismissed) }) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TypeIcon(location.type, size = 52)
                Column(Modifier.weight(1f).padding(start = 14.dp)) {
                    Text(location.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text("${stringResource(location.type.labelRes)} · ${distanceText(item.distanceKm)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(location.address, style = MaterialTheme.typography.bodyLarge)
            OpenStatus(item)
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerLow) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                    HoursRow(R.string.locator_mon_fri, location.hours.rangeOn(1), location.hours.is24Hours)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    HoursRow(R.string.locator_saturday, location.hours.rangeOn(6), location.hours.is24Hours)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    HoursRow(R.string.locator_sunday, location.hours.rangeOn(7), location.hours.is24Hours)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { onAction(LocatorViewAction.OnDirectionsClicked(location)) },
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Rounded.Directions, null, Modifier.size(18.dp))
                    Text(stringResource(R.string.locator_directions), Modifier.padding(start = 8.dp))
                }
                location.phone?.let { phone ->
                    OutlinedButton(
                        onClick = { onAction(LocatorViewAction.OnCallClicked(phone)) },
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Rounded.Call, null, Modifier.size(18.dp))
                        Text(stringResource(R.string.locator_call), Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HoursRow(@StringRes label: Int, range: TimeRange?, is24h: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(stringResource(label), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(
            when {
                is24h -> stringResource(R.string.locator_open_24h)
                range == null -> stringResource(R.string.locator_closed)
                else -> "${timeOfDayText(range.open)} – ${timeOfDayText(range.close)}"
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun distanceText(km: Double): String =
    if (km < 1) stringResource(R.string.locator_distance_m, (km * 1000).toInt())
    else stringResource(R.string.locator_distance_km, String.format(Locale.US, "%.1f", km))

@get:StringRes
private val LocationType.labelRes: Int
    get() = when (this) {
        LocationType.Branch -> R.string.locator_type_branch
        LocationType.Atm -> R.string.locator_type_atm
        LocationType.CashDeposit -> R.string.locator_type_cash_deposit
    }

private val LocationType.icon: ImageVector
    get() = when (this) {
        LocationType.Branch -> Icons.Rounded.AccountBalance
        LocationType.Atm -> Icons.Rounded.LocalAtm
        LocationType.CashDeposit -> Icons.Rounded.Payments
    }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LocationType.shape(): Shape = when (this) {
    LocationType.Branch -> MaterialShapes.Cookie9Sided
    LocationType.Atm -> MaterialShapes.Square
    LocationType.CashDeposit -> MaterialShapes.Clover4Leaf
}.toShape()

/** Any maps app; falls back to a browser link when none is installed. */
private fun Context.openDirections(location: BankLocation) {
    val (lat, lng) = location.point
    val geo = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(location.name)})"))
    if (!startSafely(geo)) {
        startSafely(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng")))
    }
}

private fun Context.startSafely(intent: Intent): Boolean = try {
    startActivity(intent)
    true
} catch (_: ActivityNotFoundException) {
    false
}
