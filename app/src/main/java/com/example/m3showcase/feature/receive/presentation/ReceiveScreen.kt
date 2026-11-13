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
import androidx.compose.ui.res.stringResource
import com.example.m3showcase.R
import com.example.m3showcase.core.ui.asString
import androidx.compose.material3.Button
import androidx.compose.material.icons.rounded.Edit

@Composable
fun ReceiveRoute(onOpenDetail: (accountNumber: String) -> Unit, viewModel: ReceiveViewModel = koinViewModel()) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ReceiveEvent.OpenDetail -> onOpenDetail(event.accountNumber)
                is ReceiveEvent.Share -> context.startActivity(
                    Intent.createChooser(
                        Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, event.text.asString(context)),
                        context.getString(R.string.receive_share_chooser),
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
        topBar = { CenterAlignedTopAppBar(colors = bankTopAppBarColors(), title = { Text(stringResource(R.string.receive_title)) }) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                ReceiveViewState.Loading -> LoadingState()
                ReceiveViewState.Empty -> EmptyState(stringResource(R.string.accounts_empty_title), stringResource(R.string.receive_empty_message))
                is ReceiveViewState.Error -> ErrorState(state.message.asString(), { onAction(ReceiveViewAction.OnRetry) }, isOffline = state.isOffline)
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

        ReceiveQrCard(qr = qr, payload = qr.payload, onClick = { onAction(ReceiveViewAction.OnQrClicked) })

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { onAction(ReceiveViewAction.OnQrClicked) }, shapes = ButtonDefaults.shapes()) {
                Icon(Icons.Rounded.Edit, null, Modifier.size(18.dp))
                Text(stringResource(R.string.receive_set_amount), Modifier.padding(start = 8.dp))
            }
        FilledTonalButton(onClick = { onAction(ReceiveViewAction.OnShareClicked) }, shapes = ButtonDefaults.shapes()) {
            Icon(Icons.Rounded.Share, null, Modifier.size(18.dp))
            Text(stringResource(R.string.receive_share_details), Modifier.padding(start = 8.dp))
        }
        }
    }
}
