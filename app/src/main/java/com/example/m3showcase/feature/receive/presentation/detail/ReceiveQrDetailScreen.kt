package com.example.m3showcase.feature.receive.presentation.detail

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.view.WindowManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.m3showcase.R
import com.example.m3showcase.core.designsystem.component.BankScreenScaffold
import com.example.m3showcase.core.designsystem.component.LoadableContent
import com.example.m3showcase.core.domain.model.CurrencyCode
import com.example.m3showcase.core.ui.asString
import com.example.m3showcase.feature.receive.presentation.ReceiveQrCard
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ReceiveQrDetailRoute(
    accountNumber: String,
    onBack: () -> Unit,
    viewModel: ReceiveQrDetailViewModel = koinViewModel(key = "receive-$accountNumber") { parametersOf(accountNumber) },
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    MaxBrightness()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ReceiveQrDetailEvent.Copy -> {
                    context.getSystemService(ClipboardManager::class.java)
                        ?.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.transfer_account_number), event.accountNumber))
                    snackbarHostState.showSnackbar(context.getString(R.string.receive_copied))
                }
                is ReceiveQrDetailEvent.Share -> context.startActivity(
                    Intent.createChooser(
                        Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, event.text.asString(context)),
                        context.getString(R.string.receive_share_chooser),
                    ),
                )
                ReceiveQrDetailEvent.NavigateBack -> onBack()
            }
        }
    }
    ReceiveQrDetailScreen(state, snackbarHostState, viewModel::onAction)
}

@Composable
fun ReceiveQrDetailScreen(state: ReceiveQrDetailViewState, snackbarHostState: SnackbarHostState, onAction: (ReceiveQrDetailViewAction) -> Unit) {
    BankScreenScaffold(
        title = stringResource(R.string.receive_detail_title),
        onBack = { onAction(ReceiveQrDetailViewAction.OnBackClicked) },
        snackbarHostState = snackbarHostState,
    ) { padding ->
        LoadableContent(state.request, onRetry = {}, modifier = Modifier.padding(padding)) { request ->
            val currency = request.qr.currency
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ReceiveQrCard(
                    qr = request.qr,
                    payload = request.payload,
                    qrSize = 240.dp,
                    amount = request.amount.takeIf { state.amountError == null },
                    note = request.note,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.LightMode, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        stringResource(R.string.receive_brightness_hint),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
                OutlinedTextField(
                    value = state.amountText,
                    onValueChange = { onAction(ReceiveQrDetailViewAction.OnAmountChanged(it)) },
                    label = { Text(stringResource(R.string.transfer_amount_label, currency.name)) },
                    prefix = { if (currency == CurrencyCode.USD) Text("$ ") },
                    suffix = { if (currency == CurrencyCode.KHR) Text(" ៛") },
                    isError = state.amountError != null,
                    supportingText = { Text(state.amountError?.asString() ?: stringResource(R.string.receive_amount_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    quickAmounts(currency).forEach { value ->
                        SuggestionChip(
                            onClick = { onAction(ReceiveQrDetailViewAction.OnQuickAmount(value)) },
                            label = { Text(if (currency == CurrencyCode.USD) "$$value" else "%,d".format(value.toLong())) },
                        )
                    }
                }
                OutlinedTextField(
                    value = state.note,
                    onValueChange = { onAction(ReceiveQrDetailViewAction.OnNoteChanged(it)) },
                    label = { Text(stringResource(R.string.receive_note)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { onAction(ReceiveQrDetailViewAction.OnCopyClicked) }, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f)) {
                        Icon(Icons.Rounded.ContentCopy, null, Modifier.size(18.dp))
                        Text(stringResource(R.string.receive_copy_number), Modifier.padding(start = 8.dp))
                    }
                    Button(onClick = { onAction(ReceiveQrDetailViewAction.OnShareClicked) }, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f)) {
                        Icon(Icons.Rounded.Share, null, Modifier.size(18.dp))
                        Text(stringResource(R.string.action_share), Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }
}

private fun quickAmounts(currency: CurrencyCode) = when (currency) {
    CurrencyCode.USD -> listOf("5", "10", "25", "50")
    CurrencyCode.KHR -> listOf("10000", "20000", "50000")
}

/** Full screen brightness while the QR is visible (scanners read it more reliably); restored on leave. */
@Composable
private fun MaxBrightness() {
    val activity = LocalContext.current.findActivity() ?: return
    DisposableEffect(activity) {
        val window = activity.window
        val previous = window.attributes.screenBrightness
        window.attributes = window.attributes.apply { screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL }
        onDispose { window.attributes = window.attributes.apply { screenBrightness = previous } }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
