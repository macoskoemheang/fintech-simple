package com.example.m3showcase.feature.payments.presentation.detail

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.m3showcase.R
import com.example.m3showcase.core.common.MoneyFormatter
import com.example.m3showcase.core.designsystem.component.BankScreenScaffold
import com.example.m3showcase.core.designsystem.component.HeroCard
import com.example.m3showcase.core.designsystem.component.InfoCard
import com.example.m3showcase.core.designsystem.component.InfoRow
import com.example.m3showcase.core.designsystem.component.LoadableContent
import com.example.m3showcase.core.designsystem.component.ServicePalettes
import com.example.m3showcase.core.ui.DateFormats
import com.example.m3showcase.core.ui.Loadable
import com.example.m3showcase.core.ui.formatAccountNumber
import com.example.m3showcase.core.ui.fullDateText
import com.example.m3showcase.feature.payments.domain.model.Payment
import com.example.m3showcase.feature.payments.domain.model.PaymentStatus
import com.example.m3showcase.feature.payments.presentation.PaymentStatusPill
import com.example.m3showcase.feature.payments.presentation.icon
import com.example.m3showcase.feature.payments.presentation.labelRes
import com.example.m3showcase.feature.payments.presentation.shape
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PaymentDetailRoute(
    paymentId: String,
    onBack: () -> Unit,
    viewModel: PaymentDetailViewModel = koinViewModel(key = "payment-$paymentId") { parametersOf(paymentId) },
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is PaymentDetailEvent.Share -> context.startActivity(
                    Intent.createChooser(
                        Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, event.payment.shareText(context)),
                        context.getString(R.string.receipt_share_chooser),
                    ),
                )
                PaymentDetailEvent.NavigateBack -> onBack()
            }
        }
    }
    PaymentDetailScreen(state, viewModel::onAction)
}

@Composable
fun PaymentDetailScreen(state: Loadable<Payment>, onAction: (PaymentDetailViewAction) -> Unit) {
    BankScreenScaffold(
        title = stringResource(R.string.payment_detail_title),
        onBack = { onAction(PaymentDetailViewAction.OnBackClicked) },
        actions = {
            if (state is Loadable.Success) {
                IconButton(onClick = { onAction(PaymentDetailViewAction.OnShareClicked) }) {
                    Icon(Icons.Rounded.Share, contentDescription = stringResource(R.string.action_share))
                }
            }
        },
    ) { padding ->
        LoadableContent(state, onRetry = { onAction(PaymentDetailViewAction.OnRetry) }, modifier = Modifier.padding(padding)) { payment ->
            PaymentDetailContent(payment, onAction)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PaymentDetailContent(payment: Payment, onAction: (PaymentDetailViewAction) -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HeroCard(
            icon = payment.category.icon,
            iconShape = payment.category.shape(),
            label = payment.biller,
            amount = MoneyFormatter.format(payment.total),
            subtitle = fullDateText(payment.timestamp),
            palette = ServicePalettes.Payment,
            badge = { PaymentStatusPill(payment.status) },
        )
        when (payment.status) {
            PaymentStatus.Pending -> Note(stringResource(R.string.payment_pending_note), MaterialTheme.colorScheme.secondaryContainer)
            PaymentStatus.Failed -> Note(stringResource(R.string.payment_failed_note), MaterialTheme.colorScheme.errorContainer)
            PaymentStatus.Success -> Unit
        }
        InfoCard(title = stringResource(R.string.payment_section_bill)) {
            InfoRow(stringResource(R.string.payment_biller), payment.biller)
            InfoRow(stringResource(R.string.payment_category), stringResource(payment.category.labelRes))
            InfoRow(stringResource(R.string.payment_customer_id), payment.customerRef, divider = false)
        }
        InfoCard(title = stringResource(R.string.payment_section_amount)) {
            InfoRow(stringResource(R.string.payment_amount), MoneyFormatter.format(payment.amount))
            InfoRow(stringResource(R.string.transfer_fee), MoneyFormatter.format(payment.fee))
            InfoRow(stringResource(R.string.payment_total), MoneyFormatter.format(payment.total), divider = false, strong = true)
        }
        InfoCard(title = stringResource(R.string.payment_section_details)) {
            InfoRow(stringResource(R.string.payment_paid_from), "${payment.fromAccountName}\n${payment.fromAccountNumber.formatAccountNumber()}")
            InfoRow(stringResource(R.string.receipt_reference), payment.reference)
            InfoRow(stringResource(R.string.receipt_date), fullDateText(payment.timestamp), divider = false)
        }
        FilledTonalButton(
            onClick = { onAction(PaymentDetailViewAction.OnShareClicked) },
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Rounded.Share, null, Modifier.size(18.dp))
            Text(stringResource(R.string.payment_share_receipt), Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun Note(text: String, container: Color) {
    Surface(shape = MaterialTheme.shapes.large, color = container, modifier = Modifier.fillMaxWidth()) {
        Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(16.dp))
    }
}

private fun Payment.shareText(context: Context): String = context.getString(
    R.string.receipt_share_text,
    biller,
    MoneyFormatter.format(total),
    DateFormats.full(context, timestamp),
    reference,
)
