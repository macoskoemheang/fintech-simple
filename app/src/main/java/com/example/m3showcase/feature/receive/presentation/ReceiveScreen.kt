package com.example.m3showcase.feature.receive.presentation

import com.example.m3showcase.core.designsystem.bankTopAppBarColors
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.m3showcase.core.designsystem.BrandColors
import com.example.m3showcase.core.designsystem.component.EmptyState
import com.example.m3showcase.core.designsystem.component.ErrorState
import com.example.m3showcase.core.designsystem.component.LoadingState
import com.example.m3showcase.core.designsystem.component.QrCode
import com.example.m3showcase.core.ui.formatAccountNumber
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReceiveRoute(viewModel: ReceiveViewModel = koinViewModel()) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ReceiveEvent.Share -> context.startActivity(
                    Intent.createChooser(
                        Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, event.text),
                        "Share account",
                    )
                )
            }
        }
    }
    ReceiveScreen(state, viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveScreen(state: ReceiveViewState, onAction: (ReceiveViewAction) -> Unit) {
    Scaffold(
        containerColor = Color.Transparent, // AppBackground shows through.
        topBar = { CenterAlignedTopAppBar(colors = bankTopAppBarColors(), title = { Text("Receive money") }) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                ReceiveViewState.Loading -> LoadingState()
                ReceiveViewState.Empty -> EmptyState("No accounts", "Open an account to receive money.")
                is ReceiveViewState.Error -> ErrorState(state.message, { onAction(ReceiveViewAction.OnRetry) }, isOffline = state.isOffline)
                is ReceiveViewState.Success -> ReceiveContent(state, onAction)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ReceiveContent(state: ReceiveViewState.Success, onAction: (ReceiveViewAction) -> Unit) {
    val qr = state.selected
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.codes.forEachIndexed { index, code ->
                FilterChip(
                    selected = index == state.selectedIndex,
                    onClick = { onAction(ReceiveViewAction.OnAccountSelected(index)) },
                    label = { Text("${code.currency.name} ·${code.accountNumber.takeLast(3)}") },
                )
            }
        }

        // The QR card stays white in dark mode too, so scanners can read it.
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = Color.White,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(BrandColors.Coral, RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("SCAN TO PAY", color = Color.White, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                }
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(qr.accountName, color = Color(0xFF181C20), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${qr.accountNumber.formatAccountNumber()} · ${qr.currency.name}",
                        color = Color(0xFF41484F),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(20.dp))
                    QrCode(content = qr.payload, size = 220.dp, color = BrandColors.NavyDeep) {
                        Surface(
                            shape = MaterialShapes.Cookie9Sided.toShape(),
                            color = BrandColors.Cyan,
                            border = BorderStroke(4.dp, Color.White),
                            modifier = Modifier.size(48.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("N", color = Color.White, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        FilledTonalButton(onClick = { onAction(ReceiveViewAction.OnShareClicked) }, shapes = ButtonDefaults.shapes()) {
            Icon(Icons.Rounded.Share, null, Modifier.size(18.dp))
            Text("Share account details", Modifier.padding(start = 8.dp))
        }
    }
}
