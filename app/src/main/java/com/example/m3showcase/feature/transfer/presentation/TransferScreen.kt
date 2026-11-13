package com.example.m3showcase.feature.transfer.presentation

import com.example.m3showcase.core.designsystem.bankTopAppBarColors
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.m3showcase.core.common.MoneyFormatter
import com.example.m3showcase.core.designsystem.component.AccountCard
import com.example.m3showcase.core.designsystem.component.CompactAccountCardWidth
import com.example.m3showcase.core.designsystem.component.EmptyState
import com.example.m3showcase.core.designsystem.component.LoadingState
import com.example.m3showcase.core.domain.model.Account
import com.example.m3showcase.core.domain.model.CurrencyCode
import com.example.m3showcase.core.domain.model.TransferRequest
import com.example.m3showcase.core.ui.formatAccountNumber
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import androidx.compose.ui.res.stringResource
import com.example.m3showcase.R
import com.example.m3showcase.core.ui.asString
import androidx.compose.ui.platform.LocalContext

@Composable
fun TransferRoute(
    fromAccountId: String?,
    onBack: () -> Unit,
    onTransferCompleted: (transactionId: String) -> Unit,
    viewModel: TransferViewModel = koinViewModel { parametersOf(fromAccountId) },
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is TransferEvent.ShowMessage -> snackbarHostState.showSnackbar(event.text.asString(context))
                is TransferEvent.OpenReceipt -> onTransferCompleted(event.transactionId)
                TransferEvent.NavigateBack -> onBack()
            }
        }
    }
    TransferScreen(state, snackbarHostState, viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TransferScreen(
    state: TransferViewState,
    snackbarHostState: SnackbarHostState,
    onAction: (TransferViewAction) -> Unit,
) {
    Scaffold(
        containerColor = Color.Transparent, // AppBackground shows through.
        topBar = {
            TopAppBar(
                colors = bankTopAppBarColors(),
                title = { Text(stringResource(R.string.transfer_title)) },
                navigationIcon = {
                    IconButton(onClick = { onAction(TransferViewAction.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
        bottomBar = {
            if (state is TransferViewState.Form) {
                Button(
                    onClick = { onAction(TransferViewAction.OnContinueClicked) },
                    enabled = state.canContinue,
                    shapes = ButtonDefaults.shapes(),
                    contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MediumContainerHeight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(16.dp)
                        .height(ButtonDefaults.MediumContainerHeight),
                ) { Text(stringResource(R.string.transfer_continue), style = MaterialTheme.typography.titleMedium) }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                TransferViewState.Loading -> LoadingState()
                TransferViewState.NoAccounts -> EmptyState(stringResource(R.string.accounts_empty_title), stringResource(R.string.transfer_no_accounts_message))
                is TransferViewState.Form -> {
                    TransferForm(state, onAction)
                    state.pendingConfirmation?.let { request ->
                        ConfirmTransferSheet(request, state.selectedAccount, state.isSubmitting, state.isOnline, onAction)
                    }
                }
            }
        }
    }
}

@Composable
private fun TransferForm(state: TransferViewState.Form, onAction: (TransferViewAction) -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AnimatedVisibility(visible = !state.isOnline) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.WifiOff, null, tint = MaterialTheme.colorScheme.onErrorContainer)
                    Text(
                        stringResource(R.string.transfer_offline),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(start = 12.dp),
                    )
                }
            }
        }

        SectionLabel(stringResource(R.string.transfer_from_account))
        SourceAccountPicker(state.accounts, state.selectedAccount?.id, onAction)

        SectionLabel(stringResource(R.string.transfer_to_account))
        OutlinedTextField(
            value = state.recipient,
            onValueChange = { onAction(TransferViewAction.OnRecipientChanged(it)) },
            label = { Text(stringResource(R.string.transfer_account_number)) },
            placeholder = { Text("000 000 000") },
            isError = state.recipientError != null,
            supportingText = { Text(state.recipientError?.asString() ?: stringResource(R.string.transfer_account_number_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )

        val currency = state.selectedAccount?.balance?.currency ?: CurrencyCode.USD
        SectionLabel(stringResource(R.string.transfer_amount))
        OutlinedTextField(
            value = state.amount,
            onValueChange = { onAction(TransferViewAction.OnAmountChanged(it)) },
            label = { Text(stringResource(R.string.transfer_amount_label, currency.name)) },
            prefix = { if (currency == CurrencyCode.USD) Text("$ ") },
            suffix = { if (currency == CurrencyCode.KHR) Text(" ៛") },
            isError = state.amountError != null,
            supportingText = {
                Text(state.amountError?.asString() ?: state.selectedAccount?.let { stringResource(R.string.transfer_available, MoneyFormatter.format(it.balance)) }.orEmpty())
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
            textStyle = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            quickAmounts(currency).forEach { value ->
                SuggestionChip(
                    onClick = { onAction(TransferViewAction.OnQuickAmountSelected(value)) },
                    label = { Text(if (currency == CurrencyCode.USD) "$$value" else "%,d".format(value.toLong())) },
                )
            }
        }

        OutlinedTextField(
            value = state.note,
            onValueChange = { onAction(TransferViewAction.OnNoteChanged(it)) },
            label = { Text(stringResource(R.string.transfer_remark_optional)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SectionLabel(text: String) = Text(
    text,
    style = MaterialTheme.typography.titleSmall,
    color = MaterialTheme.colorScheme.primary,
    modifier = Modifier.padding(horizontal = 20.dp),
)

/**
 * Horizontal picker of source accounts as compact bank cards.
 * The selected card stays full size with a ring and check; the others shrink and dim.
 * The selected card is kept in view, including one preselected from "Transfer" on My Accounts.
 */
@Composable
private fun SourceAccountPicker(accounts: List<Account>, selectedId: String?, onAction: (TransferViewAction) -> Unit) {
    val listState = rememberLazyListState()
    val haptics = LocalHapticFeedback.current
    var isFirstScroll by remember { mutableStateOf(true) }
    // Keep the selected card fully in view: jump there on first show, glide there after a tap.
    LaunchedEffect(selectedId) {
        val index = accounts.indexOfFirst { it.id == selectedId }
        if (index < 0) return@LaunchedEffect
        if (isFirstScroll) listState.scrollToItem(index) else listState.animateScrollToItem(index)
        isFirstScroll = false
    }
    LazyRow(
        state = listState,
        // Vertical room so the shadow and ring are not clipped.
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(accounts, key = { it.id }) { account ->
            val selected = account.id == selectedId
            AccountCard(
                account = account,
                balanceText = MoneyFormatter.format(account.balance),
                onClick = {
                    if (!selected) {
                        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        onAction(TransferViewAction.OnAccountSelected(account.id))
                    }
                },
                compact = true,
                selected = selected,
                modifier = Modifier.width(CompactAccountCardWidth),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ConfirmTransferSheet(
    request: TransferRequest,
    from: Account?,
    isSubmitting: Boolean,
    isOnline: Boolean,
    onAction: (TransferViewAction) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = { if (!isSubmitting) onAction(TransferViewAction.OnConfirmDismissed) },
        sheetState = sheetState,
    ) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.transfer_confirm_title), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                MoneyFormatter.format(request.amount),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(16.dp))
            DetailRow(stringResource(R.string.transfer_from), "${from?.name.orEmpty()} · ${from?.number?.formatAccountNumber().orEmpty()}")
            HorizontalDivider()
            DetailRow(stringResource(R.string.transfer_to), request.toAccountNumber.formatAccountNumber())
            HorizontalDivider()
            DetailRow(stringResource(R.string.transfer_fee), MoneyFormatter.format(request.amount.copy(minor = 0)))
            request.note?.let {
                HorizontalDivider()
                DetailRow(stringResource(R.string.transfer_remark), it)
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onAction(TransferViewAction.OnConfirmClicked) },
                enabled = !isSubmitting && isOnline,
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier.fillMaxWidth().height(ButtonDefaults.MediumContainerHeight),
            ) {
                if (isSubmitting) {
                    LoadingIndicator(Modifier.size(28.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(stringResource(if (isOnline) R.string.transfer_confirm_send else R.string.transfer_waiting_connection), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.Medium)
    }
}

private fun quickAmounts(currency: CurrencyCode) = when (currency) {
    CurrencyCode.USD -> listOf("10", "20", "50", "100")
    CurrencyCode.KHR -> listOf("10000", "20000", "50000", "100000")
}
