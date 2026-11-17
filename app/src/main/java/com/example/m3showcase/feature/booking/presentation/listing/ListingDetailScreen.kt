package com.example.m3showcase.feature.booking.presentation.listing

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.m3showcase.R
import com.example.m3showcase.core.common.MoneyFormatter
import com.example.m3showcase.core.designsystem.component.BankScreenScaffold
import com.example.m3showcase.core.designsystem.component.InfoCard
import com.example.m3showcase.core.designsystem.component.InfoRow
import com.example.m3showcase.core.designsystem.component.LoadableContent
import com.example.m3showcase.core.designsystem.component.StatusPill
import com.example.m3showcase.core.domain.model.CalendarDate
import com.example.m3showcase.core.domain.model.calendarDateOfEpochDay
import com.example.m3showcase.core.domain.model.toEpochDay
import com.example.m3showcase.core.ui.Loadable
import com.example.m3showcase.core.ui.asString
import com.example.m3showcase.core.ui.dateText
import com.example.m3showcase.feature.booking.domain.model.BookingQuote
import com.example.m3showcase.feature.booking.domain.model.CancellationPolicy
import com.example.m3showcase.feature.booking.domain.model.Listing
import com.example.m3showcase.feature.booking.domain.model.PriceUnit
import com.example.m3showcase.feature.booking.presentation.CategoryBadge
import com.example.m3showcase.feature.booking.presentation.colors
import com.example.m3showcase.feature.booking.presentation.formatRating
import com.example.m3showcase.feature.booking.presentation.icon
import com.example.m3showcase.feature.booking.presentation.labelRes
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ListingDetailRoute(
    listingId: String,
    onBack: () -> Unit,
    onBooked: (String) -> Unit,
    viewModel: ListingDetailViewModel = koinViewModel(key = "listing-$listingId") { parametersOf(listingId) },
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ListingDetailEvent.NavigateBack -> onBack()
                is ListingDetailEvent.BookingCreated -> onBooked(event.bookingId)
                is ListingDetailEvent.ShowMessage -> snackbarHostState.showSnackbar(event.text.asString(context))
            }
        }
    }
    ListingDetailScreen(state, snackbarHostState, viewModel::onAction)
}

@Composable
fun ListingDetailScreen(state: ListingDetailViewState, snackbarHostState: SnackbarHostState, onAction: (ListingDetailViewAction) -> Unit) {
    BankScreenScaffold(
        title = stringResource(R.string.booking_details),
        onBack = { onAction(ListingDetailViewAction.OnBackClicked) },
        snackbarHostState = snackbarHostState,
    ) { padding ->
        LoadableContent(state.listing, onRetry = { onAction(ListingDetailViewAction.OnRetry) }, modifier = Modifier.padding(padding)) { listing ->
            ListingDetailContent(listing, state, onAction)
        }
    }
    if (state.showDatePicker) CheckInDatePicker(state.checkIn, state.today, onAction)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ListingDetailContent(listing: Listing, state: ListingDetailViewState, onAction: (ListingDetailViewAction) -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ListingHeader(listing)

        InfoCard(title = stringResource(R.string.booking_about)) {
            Text(listing.description, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 12.dp))
        }

        if (listing.amenities.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle(stringResource(R.string.booking_amenities))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listing.amenities.forEach { amenity ->
                        AssistChip(
                            onClick = {},
                            label = { Text(stringResource(amenity.labelRes)) },
                            leadingIcon = { Icon(amenity.icon, null, Modifier.size(AssistChipDefaults.IconSize)) },
                        )
                    }
                }
            }
        }

        TripCard(listing, state, onAction)
        PriceCard(state.quote, listing, onRetry = { onAction(ListingDetailViewAction.OnRetryQuote) })

        OutlinedTextField(
            value = state.guestName,
            onValueChange = { onAction(ListingDetailViewAction.OnGuestNameChanged(it)) },
            label = { Text(stringResource(R.string.booking_guest_name)) },
            singleLine = true,
            isError = state.guestName.isBlank(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.note,
            onValueChange = { onAction(ListingDetailViewAction.OnNoteChanged(it)) },
            label = { Text(stringResource(R.string.booking_note)) },
            minLines = 2,
            maxLines = 4,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth(),
        )

        BookButton(state, onClick = { onAction(ListingDetailViewAction.OnBookClicked) })
    }
}

@Composable
private fun ListingHeader(listing: Listing) {
    val colors = listing.category.colors()
    Surface(shape = MaterialTheme.shapes.extraLarge, color = colors.container, contentColor = colors.content, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryBadge(listing.category, size = 64.dp)
                Spacer(Modifier.weight(1f))
                val scheme = MaterialTheme.colorScheme
                if (listing.cancellation == CancellationPolicy.Free48h) {
                    StatusPill(stringResource(R.string.booking_free_cancel), scheme.surface, scheme.primary)
                } else {
                    StatusPill(stringResource(R.string.booking_non_refundable), scheme.surface, scheme.error)
                }
            }
            Spacer(Modifier.size(4.dp))
            Text(stringResource(listing.category.labelRes), style = MaterialTheme.typography.labelLarge)
            Text(listing.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.LocationOn, null, Modifier.size(16.dp))
                Text(" ${listing.address}, ${listing.city}", style = MaterialTheme.typography.bodyMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Star, null, Modifier.size(16.dp))
                Text(" " + stringResource(R.string.booking_rating_reviews, formatRating(listing.rating), listing.reviewCount), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun TripCard(listing: Listing, state: ListingDetailViewState, onAction: (ListingDetailViewAction) -> Unit) {
    val perNight = listing.unit == PriceUnit.Night
    InfoCard(title = stringResource(R.string.booking_your_trip)) {
        Surface(onClick = { onAction(ListingDetailViewAction.OnCheckInClicked) }, color = Color.Transparent) {
            TripRow(
                icon = Icons.Rounded.CalendarMonth,
                label = stringResource(if (perNight) R.string.booking_check_in else R.string.booking_date),
                value = dateText(state.checkIn),
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        if (perNight) {
            StepperRow(
                icon = Icons.Rounded.NightsStay,
                label = stringResource(R.string.booking_nights),
                value = state.nights,
                canDecrease = state.nights > 1,
                canIncrease = state.nights < 30,
                onChange = { onAction(ListingDetailViewAction.OnNightsChanged(it)) },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }
        StepperRow(
            icon = Icons.Rounded.Group,
            label = stringResource(R.string.booking_guests),
            supporting = stringResource(R.string.booking_max_guests, listing.maxGuests),
            value = state.guests,
            canDecrease = state.guests > 1,
            canIncrease = state.guests < listing.maxGuests,
            onChange = { onAction(ListingDetailViewAction.OnGuestsChanged(it)) },
        )
    }
}

@Composable
private fun TripRow(icon: ImageVector, label: String, value: String) {
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f).padding(start = 12.dp))
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StepperRow(
    icon: ImageVector,
    label: String,
    value: Int,
    canDecrease: Boolean,
    canIncrease: Boolean,
    onChange: (Int) -> Unit,
    supporting: String? = null,
) {
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            supporting?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        FilledTonalIconButton(onClick = { onChange(-1) }, enabled = canDecrease, shapes = IconButtonDefaults.shapes()) {
            Icon(Icons.Rounded.Remove, stringResource(R.string.booking_decrease))
        }
        AnimatedContent(value, label = "stepper") { shown ->
            Text(
                shown.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(min = 40.dp),
            )
        }
        FilledTonalIconButton(onClick = { onChange(+1) }, enabled = canIncrease, shapes = IconButtonDefaults.shapes()) {
            Icon(Icons.Rounded.Add, stringResource(R.string.booking_increase))
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PriceCard(quote: Loadable<BookingQuote>, listing: Listing, onRetry: () -> Unit) {
    InfoCard(title = stringResource(R.string.booking_price)) {
        when (quote) {
            Loadable.Loading -> Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                LoadingIndicator(Modifier.size(32.dp))
                Text(stringResource(R.string.booking_checking), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 12.dp))
            }
            is Loadable.Error -> Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(quote.message.asString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                TextButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
            }
            is Loadable.Success -> {
                val q = quote.data
                if (!q.available) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.EventBusy, null, tint = MaterialTheme.colorScheme.error)
                        Text(
                            stringResource(R.string.booking_unavailable, q.unavailableDate?.let { dateText(it) }.orEmpty()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 12.dp),
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
                val count = if (listing.unit == PriceUnit.Night) q.nights else q.guests
                InfoRow(stringResource(R.string.booking_price_line, MoneyFormatter.format(q.unitPrice), count), MoneyFormatter.format(q.subtotal))
                InfoRow(stringResource(R.string.booking_tax), MoneyFormatter.format(q.tax))
                InfoRow(stringResource(R.string.booking_total), MoneyFormatter.format(q.total), divider = false, strong = true)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BookButton(state: ListingDetailViewState, onClick: () -> Unit) {
    val total = (state.quote as? Loadable.Success)?.data?.takeIf { it.available }?.total
    Button(
        onClick = onClick,
        enabled = state.canBook,
        shapes = ButtonDefaults.shapes(),
        contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MediumContainerHeight),
        modifier = Modifier.fillMaxWidth().heightIn(min = ButtonDefaults.MediumContainerHeight),
    ) {
        if (state.isBooking) {
            LoadingIndicator(Modifier.size(28.dp), color = MaterialTheme.colorScheme.onPrimary)
        } else {
            Text(
                if (total != null) "${stringResource(R.string.booking_book_now)} · ${MoneyFormatter.format(total)}" else stringResource(R.string.booking_book_now),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 4.dp))
}

private const val DayMillis = 86_400_000L

private fun CalendarDate.toUtcMillis(): Long = toEpochDay() * DayMillis

/** The Material date picker works in UTC midnight millis. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckInDatePicker(selected: CalendarDate, today: CalendarDate, onAction: (ListingDetailViewAction) -> Unit) {
    val firstDay = today.toUtcMillis()
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = selected.toUtcMillis(),
        yearRange = today.year..today.year + 1,
        selectableDates = remember(firstDay) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= firstDay
                override fun isSelectableYear(year: Int) = year >= today.year
            }
        },
    )
    DatePickerDialog(
        onDismissRequest = { onAction(ListingDetailViewAction.OnDatePickerDismissed) },
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onAction(ListingDetailViewAction.OnCheckInSelected(calendarDateOfEpochDay(Math.floorDiv(millis, DayMillis))))
                    } ?: onAction(ListingDetailViewAction.OnDatePickerDismissed)
                },
            ) { Text(stringResource(R.string.action_done)) }
        },
        dismissButton = {
            TextButton(onClick = { onAction(ListingDetailViewAction.OnDatePickerDismissed) }) { Text(stringResource(R.string.action_cancel)) }
        },
    ) {
        DatePicker(state = pickerState)
    }
}
