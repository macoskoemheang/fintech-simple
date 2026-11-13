package com.example.m3showcase.feature.receipt.presentation

import com.example.m3showcase.core.designsystem.bankTopAppBarColors
import androidx.compose.ui.graphics.Color
import android.content.Intent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.m3showcase.core.common.MoneyFormatter
import com.example.m3showcase.core.designsystem.BankTheme
import com.example.m3showcase.core.designsystem.component.ErrorState
import com.example.m3showcase.core.designsystem.component.LoadingState
import com.example.m3showcase.core.domain.model.Transaction
import com.example.m3showcase.core.ui.DateFormats
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun ReceiptRoute(
    transactionId: String,
    isNewTransfer: Boolean,
    onClose: () -> Unit,
    viewModel: ReceiptViewModel = koinViewModel(key = "receipt-$transactionId") {
        parametersOf(transactionId, isNewTransfer)
    },
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ReceiptEvent.Close -> onClose()
                is ReceiptEvent.Share -> context.startActivity(
                    Intent.createChooser(
                        Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, event.text),
                        "Share receipt",
                    )
                )
            }
        }
    }
    ReceiptScreen(state, viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(state: ReceiptViewState, onAction: (ReceiptViewAction) -> Unit) {
    Scaffold(
        containerColor = Color.Transparent, // AppBackground shows through.
        topBar = {
            TopAppBar(
                colors = bankTopAppBarColors(),
                title = { Text(if ((state as? ReceiptViewState.Success)?.isNewTransfer == true) "" else "Transaction details") },
                navigationIcon = {
                    IconButton(onClick = { onAction(ReceiptViewAction.OnDoneClicked) }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    if (state is ReceiptViewState.Success) {
                        IconButton(onClick = { onAction(ReceiptViewAction.OnShareClicked) }) {
                            Icon(Icons.Rounded.Share, contentDescription = "Share")
                        }
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                ReceiptViewState.Loading -> LoadingState()
                is ReceiptViewState.Error -> ErrorState(state.message, { onAction(ReceiptViewAction.OnRetry) })
                is ReceiptViewState.Success -> ReceiptContent(state.transaction, state.isNewTransfer, onAction)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ReceiptContent(tx: Transaction, isNewTransfer: Boolean, onAction: (ReceiptViewAction) -> Unit) {
    // Expressive entrance: the badge springs in with overshoot and a small spin.
    val scale = remember { Animatable(0.3f) }
    val rotation = remember { Animatable(-90f) }
    LaunchedEffect(Unit) {
        coroutineScope {
            launch { scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }
            launch { rotation.animateTo(0f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)) }
        }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            shape = MaterialShapes.Sunny.toShape(),
            color = if (tx.isCredit || isNewTransfer) BankTheme.colors.credit else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(104.dp).scale(scale.value).rotate(rotation.value),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Check, null, Modifier.size(52.dp), tint = MaterialTheme.colorScheme.surface)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            if (isNewTransfer) "Transfer successful" else tx.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            MoneyFormatter.format(tx.amount, withSign = true),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = if (tx.isCredit) BankTheme.colors.credit else MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                ReceiptRow("Description", tx.title)
                HorizontalDivider()
                ReceiptRow(if (tx.isCredit) "From" else "To", tx.counterparty)
                HorizontalDivider()
                ReceiptRow("Date", DateFormats.full(tx.timestampMillis))
                HorizontalDivider()
                ReceiptRow("Reference", tx.id.take(12).uppercase())
                tx.note?.let {
                    HorizontalDivider()
                    ReceiptRow("Remark", it)
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { onAction(ReceiptViewAction.OnShareClicked) },
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier.weight(1f).height(ButtonDefaults.MediumContainerHeight),
            ) { Text("Share") }
            Button(
                onClick = { onAction(ReceiptViewAction.OnDoneClicked) },
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier.weight(1f).height(ButtonDefaults.MediumContainerHeight),
            ) { Text("Done") }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.Medium)
    }
}
