package com.example.m3showcase.feature.payments.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.m3showcase.R
import com.example.m3showcase.core.common.MoneyFormatter
import com.example.m3showcase.core.designsystem.component.BankScreenScaffold
import com.example.m3showcase.core.designsystem.component.EmptyState
import com.example.m3showcase.core.designsystem.component.PagedLazyColumn
import com.example.m3showcase.core.designsystem.component.SummaryTile
import com.example.m3showcase.core.ui.Loadable
import com.example.m3showcase.core.ui.asString
import com.example.m3showcase.core.ui.relativeDayText
import com.example.m3showcase.feature.payments.domain.model.BillerCategory
import com.example.m3showcase.feature.payments.domain.model.Payment
import com.example.m3showcase.feature.payments.domain.model.PaymentStatus
import com.example.m3showcase.feature.payments.presentation.PaymentStatusPill
import com.example.m3showcase.feature.payments.presentation.icon
import com.example.m3showcase.feature.payments.presentation.labelRes
import com.example.m3showcase.feature.payments.presentation.shape
import org.koin.androidx.compose.koinViewModel

@Composable
fun PaymentsRoute(onBack: () -> Unit, onOpenPayment: (String) -> Unit, viewModel: PaymentsViewModel = koinViewModel()) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is PaymentsEvent.ShowMessage -> snackbarHostState.showSnackbar(event.text.asString(context))
                is PaymentsEvent.OpenPayment -> onOpenPayment(event.id)
                PaymentsEvent.NavigateBack -> onBack()
            }
        }
    }
    PaymentsScreen(state, snackbarHostState, viewModel::onAction)
}

@Composable
fun PaymentsScreen(state: PaymentsViewState, snackbarHostState: SnackbarHostState, onAction: (PaymentsViewAction) -> Unit) {
    BankScreenScaffold(
        title = stringResource(R.string.payments_title),
        onBack = { onAction(PaymentsViewAction.OnBackClicked) },
        snackbarHostState = snackbarHostState,
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            CategoryChips(state.category) { onAction(PaymentsViewAction.OnCategorySelected(it)) }
            PagedLazyColumn(
                state = state.paging,
                onRefresh = { onAction(PaymentsViewAction.OnRefresh) },
                onLoadMore = { onAction(PaymentsViewAction.OnLoadMore) },
                onRetry = { onAction(PaymentsViewAction.OnRetry) },
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                endLabel = { pluralStringResource(R.plurals.payments_end, it, it) },
                emptyContent = {
                    EmptyState(stringResource(R.string.payments_empty_title), stringResource(R.string.payments_empty_message), icon = Icons.Outlined.ReceiptLong)
                },
                header = {
                    (state.summary as? Loadable.Success)?.data?.let { summary ->
                        item(key = "summary", contentType = "summary") {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                                SummaryTile(stringResource(R.string.payments_paid_this_month), MoneyFormatter.format(summary.paidThisMonth), Modifier.weight(1f))
                                SummaryTile(stringResource(R.string.payment_status_pending), summary.pendingCount.toString(), Modifier.weight(1f))
                            }
                        }
                    }
                },
            ) { items ->
                items(items.size, key = { items[it].id }, contentType = { "payment" }) { index ->
                    val payment = items[index]
                    PaymentRow(payment, { onAction(PaymentsViewAction.OnPaymentClicked(payment.id)) }, Modifier.animateItem())
                }
            }
        }
    }
}

@Composable
private fun CategoryChips(selected: BillerCategory?, onSelect: (BillerCategory?) -> Unit) {
    val options = listOf<BillerCategory?>(null) + BillerCategory.entries
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
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
private fun PaymentRow(payment: Payment, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.94f),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = payment.category.shape(),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(44.dp),
            ) { Box(contentAlignment = Alignment.Center) { Icon(payment.category.icon, null, Modifier.size(22.dp)) } }
            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text(payment.biller, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${stringResource(payment.category.labelRes)} · ${relativeDayText(payment.timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    MoneyFormatter.format(payment.total),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (payment.status == PaymentStatus.Failed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
                if (payment.status != PaymentStatus.Success) {
                    Box(Modifier.padding(top = 4.dp)) { PaymentStatusPill(payment.status) }
                }
            }
        }
    }
}
