package com.example.m3showcase.feature.accounts.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.m3showcase.core.common.MoneyFormatter
import com.example.m3showcase.core.designsystem.component.EmptyState
import com.example.m3showcase.core.designsystem.component.ErrorState
import com.example.m3showcase.core.designsystem.component.LoadingState
import com.example.m3showcase.core.domain.model.Account
import com.example.m3showcase.core.domain.model.AccountType
import com.example.m3showcase.core.ui.formatAccountNumber
import org.koin.androidx.compose.koinViewModel

@Composable
fun AccountsRoute(
    onOpenHistory: (accountId: String) -> Unit,
    onOpenTransfer: (accountId: String) -> Unit,
    viewModel: AccountsViewModel = koinViewModel(),
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is AccountsEvent.ShowMessage -> snackbarHostState.showSnackbar(event.text)
                is AccountsEvent.OpenHistory -> onOpenHistory(event.accountId)
                is AccountsEvent.OpenTransfer -> onOpenTransfer(event.accountId)
            }
        }
    }
    AccountsScreen(state, snackbarHostState, viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AccountsScreen(
    state: AccountsViewState,
    snackbarHostState: SnackbarHostState,
    onAction: (AccountsViewAction) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("My Accounts") },
                subtitle = { Text("Savings and current accounts") },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                AccountsViewState.Loading -> LoadingState()
                AccountsViewState.Empty -> EmptyState("No accounts", "Open an account at any branch to get started.")
                is AccountsViewState.Error -> ErrorState(state.message, { onAction(AccountsViewAction.OnRefresh) }, isOffline = state.isOffline)
                is AccountsViewState.Success -> PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { onAction(AccountsViewAction.OnRefresh) },
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(state.accounts, key = { it.id }) { account ->
                            AccountRow(account, state.isBalanceHidden, onAction)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AccountRow(account: Account, hidden: Boolean, onAction: (AccountsViewAction) -> Unit) {
    ElevatedCard(
        onClick = { onAction(AccountsViewAction.OnAccountClicked(account.id)) },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = MaterialShapes.Cookie9Sided.toShape(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.AccountBalanceWallet, null) }
                }
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(account.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${account.type.label()} · ${account.number.formatAccountNumber()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Available balance", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (hidden) MoneyFormatter.masked(account.balance.currency) else MoneyFormatter.format(account.balance),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                FilledTonalButton(
                    onClick = { onAction(AccountsViewAction.OnTransferClicked(account.id)) },
                    shapes = ButtonDefaults.shapes(),
                ) {
                    Icon(Icons.Rounded.SwapHoriz, null, Modifier.size(18.dp))
                    Text("Transfer", Modifier.padding(start = 6.dp))
                }
            }
        }
    }
}

private fun AccountType.label() = when (this) {
    AccountType.Savings -> "Savings"
    AccountType.Current -> "Current"
    AccountType.FixedDeposit -> "Fixed deposit"
}
