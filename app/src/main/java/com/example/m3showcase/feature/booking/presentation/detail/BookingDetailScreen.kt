package com.example.m3showcase.feature.booking.presentation.detail

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.m3showcase.R
import com.example.m3showcase.core.common.MoneyFormatter
import com.example.m3showcase.core.designsystem.component.BankScreenScaffold
import com.example.m3showcase.core.designsystem.component.InfoCard
import com.example.m3showcase.core.designsystem.component.InfoRow
import com.example.m3showcase.core.designsystem.component.LoadableContent
import com.example.m3showcase.core.ui.Loadable
import com.example.m3showcase.core.ui.asString
import com.example.m3showcase.core.ui.dateText
import com.example.m3showcase.feature.booking.domain.model.Booking
import com.example.m3showcase.feature.booking.domain.model.BookingStatus
import com.example.m3showcase.core.domain.model.Money
import com.example.m3showcase.feature.booking.domain.model.PriceUnit
import com.example.m3showcase.feature.booking.presentation.BookingStatusPill
import com.example.m3showcase.feature.booking.presentation.CategoryBadge
import com.example.m3showcase.feature.booking.presentation.labelRes
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun BookingDetailRoute(
    bookingId: String,
    isNew: Boolean,
    onBack: () -> Unit,
    viewModel: BookingDetailViewModel = koinViewModel(key = "booking-$bookingId") { parametersOf(bookingId, isNew) },
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                BookingDetailEvent.NavigateBack -> onBack()
                is BookingDetailEvent.ShowMessage -> snackbarHostState.showSnackbar(event.text.asString(context))
            }
        }
    }
    BookingDetailScreen(state, snackbarHostState, viewModel::onAction)
}

@Composable
fun BookingDetailScreen(state: BookingDetailViewState, snackbarHostState: SnackbarHostState, onAction: (BookingDetailViewAction) -> Unit) {
    BankScreenScaffold(
        title = stringResource(R.string.booking_detail_title),
        onBack = { onAction(BookingDetailViewAction.OnBackClicked) },
        snackbarHostState = snackbarHostState,
    ) { padding ->
        LoadableContent(state.booking, onRetry = { onAction(BookingDetailViewAction.OnRetry) }, modifier = Modifier.padding(padding)) { booking ->
            BookingDetailContent(booking, state, onAction)
        }
    }
    val booking = (state.booking as? Loadable.Success)?.data
    if (state.showCancelDialog && booking != null) CancelDialog(state.refundIfCancelled(booking), onAction)
}

@Composable
private fun BookingDetailContent(booking: Booking, state: BookingDetailViewState, onAction: (BookingDetailViewAction) -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (state.isNew && booking.status == BookingStatus.Confirmed) ConfirmedBanner()
        ConfirmationCard(booking)

        val perNight = booking.listing.unit == PriceUnit.Night
        InfoCard(title = stringResource(R.string.booking_stay)) {
            InfoRow(stringResource(if (perNight) R.string.booking_check_in else R.string.booking_date), dateText(booking.checkIn))
            if (perNight) {
                InfoRow(stringResource(R.string.booking_check_out), dateText(booking.checkOut))
                InfoRow(stringResource(R.string.booking_nights), pluralStringResource(R.plurals.booking_nights_count, booking.nights, booking.nights))
            }
            InfoRow(stringResource(R.string.booking_guests), pluralStringResource(R.plurals.booking_guests_count, booking.guests, booking.guests))
            InfoRow(stringResource(R.string.booking_guest_name), booking.guestName)
            if (booking.note.isNotBlank()) InfoRow(stringResource(R.string.booking_note_short), booking.note)
            InfoRow(stringResource(R.string.booking_booked_on), dateText(booking.bookedOn), divider = false)
        }

        InfoCard(title = stringResource(R.string.booking_price)) {
            InfoRow(stringResource(R.string.booking_subtotal), MoneyFormatter.format(booking.subtotal))
            InfoRow(stringResource(R.string.booking_tax), MoneyFormatter.format(booking.tax))
            InfoRow(stringResource(R.string.booking_total), MoneyFormatter.format(booking.total), divider = booking.refund != null, strong = true)
            booking.refund?.let {
                InfoRow(stringResource(R.string.booking_refund), MoneyFormatter.format(it), divider = false, valueColor = MaterialTheme.colorScheme.primary, strong = true)
            }
        }

        if (booking.status == BookingStatus.Confirmed) CancelButton(state.isCancelling) { onAction(BookingDetailViewAction.OnCancelClicked) }
    }
}

/** A slowly turning Expressive shape with a check: the "you're booked" moment. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ConfirmedBanner() {
    val rotation by rememberInfiniteTransition(label = "confirmed").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(12_000, easing = LinearEasing), RepeatMode.Restart),
        label = "rotation",
    )
    Column(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(96.dp), contentAlignment = Alignment.Center) {
            Surface(shape = MaterialShapes.SoftBurst.toShape(), color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(96.dp).rotate(rotation)) {}
            Icon(Icons.Rounded.Check, null, Modifier.size(44.dp), tint = MaterialTheme.colorScheme.onPrimary)
        }
        Spacer(Modifier.size(12.dp))
        Text(stringResource(R.string.booking_confirmed_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            stringResource(R.string.booking_confirmed_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ConfirmationCard(booking: Booking) {
    Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.94f), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryBadge(booking.listing.category, size = 48.dp)
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(booking.listing.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${stringResource(booking.listing.category.labelRes)} · ${booking.listing.city}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                BookingStatusPill(booking.status)
            }
            Text(stringResource(R.string.booking_confirmation_code), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                booking.confirmationCode,
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (booking.status == BookingStatus.Cancelled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CancelButton(isCancelling: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = !isCancelling,
        shapes = ButtonDefaults.shapes(),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        modifier = Modifier.fillMaxWidth().heightIn(min = ButtonDefaults.MediumContainerHeight),
    ) {
        if (isCancelling) {
            LoadingIndicator(Modifier.size(28.dp))
        } else {
            Icon(Icons.Rounded.EventBusy, null)
            Text(stringResource(R.string.booking_cancel), Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun CancelDialog(refund: Money, onAction: (BookingDetailViewAction) -> Unit) {
    AlertDialog(
        onDismissRequest = { onAction(BookingDetailViewAction.OnCancelDismissed) },
        icon = { Icon(Icons.Rounded.EventBusy, null) },
        title = { Text(stringResource(R.string.booking_cancel_title)) },
        text = {
            Text(
                if (refund.minor > 0) {
                    stringResource(R.string.booking_cancel_message_free, MoneyFormatter.format(refund))
                } else {
                    stringResource(R.string.booking_cancel_message_no_refund)
                },
            )
        },
        confirmButton = {
            TextButton(onClick = { onAction(BookingDetailViewAction.OnCancelConfirmed) }) {
                Text(stringResource(R.string.booking_cancel), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = { onAction(BookingDetailViewAction.OnCancelDismissed) }) { Text(stringResource(R.string.booking_keep)) } },
    )
}
