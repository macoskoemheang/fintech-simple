package com.example.m3showcase.feature.accounts.presentation.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.m3showcase.core.common.MoneyFormatter
import com.example.m3showcase.core.designsystem.component.EmptyState
import com.example.m3showcase.core.designsystem.component.ErrorState
import com.example.m3showcase.core.designsystem.component.LoadingState
import com.example.m3showcase.core.designsystem.component.TransactionItem
import com.example.m3showcase.feature.accounts.domain.model.TransactionFilter
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun TransactionHistoryRoute(
    accountId: String?,
    onBack: () -> Unit,
    onOpenReceipt: (String) -> Unit,
    // Navigation 3 gives each entry its own ViewModelStore, so this ViewModel is
    // scoped to this back stack entry and created with the key's argument.
    viewModel: TransactionHistoryViewModel = koinViewModel(key = "history-$accountId") { parametersOf(accountId) },
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is TransactionHistoryEvent.ShowMessage -> snackbarHostState.showSnackbar(event.text)
                is TransactionHistoryEvent.OpenReceipt -> onOpenReceipt(event.transactionId)
                TransactionHistoryEvent.NavigateBack -> onBack()
            }
        }
    }
    TransactionHistoryScreen(state, snackbarHostState, viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    state: TransactionHistoryViewState,
    snackbarHostState: SnackbarHostState,
    onAction: (TransactionHistoryViewAction) -> Unit,
) {
    val account = (state as? TransactionHistoryViewState.Success)?.history?.account
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(account?.name ?: "All transactions")
                        if (account != null) {
                            val hidden = (state as TransactionHistoryViewState.Success).history.isBalanceHidden
                            Text(
                                if (hidden) MoneyFormatter.masked(account.balance.currency) else MoneyFormatter.format(account.balance),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(TransactionHistoryViewAction.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            FilterGroup(state.filter) { onAction(TransactionHistoryViewAction.OnFilterSelected(it)) }
            Box(Modifier.weight(1f)) {
                when (state) {
                    is TransactionHistoryViewState.Loading -> LoadingState()
                    is TransactionHistoryViewState.Error ->
                        ErrorState(state.message, { onAction(TransactionHistoryViewAction.OnRefresh) }, isOffline = state.isOffline)
                    is TransactionHistoryViewState.Success -> PullToRefreshBox(
                        isRefreshing = state.isRefreshing,
                        onRefresh = { onAction(TransactionHistoryViewAction.OnRefresh) },
                    ) {
                        if (state.history.transactions.isEmpty()) {
                            EmptyState("Nothing here", "No transactions match this filter.")
                        } else {
                            LazyColumn(Modifier.fillMaxSize()) {
                                items(state.history.transactions, key = { it.id }) { tx ->
                                    TransactionItem(
                                        transaction = tx,
                                        hideAmount = state.history.isBalanceHidden,
                                        onClick = { onAction(TransactionHistoryViewAction.OnTransactionClicked(tx.id)) },
                                        modifier = Modifier.animateItem(),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Expressive connected button group used as a single-select filter. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FilterGroup(selected: TransactionFilter, onSelect: (TransactionFilter) -> Unit) {
    val options = TransactionFilter.entries
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        options.forEachIndexed { index, filter ->
            ToggleButton(
                checked = selected == filter,
                onCheckedChange = { onSelect(filter) },
                modifier = Modifier.weight(1f).semantics { role = Role.RadioButton },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
            ) {
                Text(
                    when (filter) {
                        TransactionFilter.All -> "All"
                        TransactionFilter.MoneyIn -> "Money in"
                        TransactionFilter.MoneyOut -> "Money out"
                    }
                )
            }
        }
    }
}
