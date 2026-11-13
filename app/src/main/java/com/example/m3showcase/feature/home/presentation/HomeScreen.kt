package com.example.m3showcase.feature.home.presentation

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.m3showcase.core.designsystem.component.EmptyState
import com.example.m3showcase.core.designsystem.component.ErrorState
import com.example.m3showcase.core.designsystem.component.LoadingState
import com.example.m3showcase.core.designsystem.component.TransactionItem
import com.example.m3showcase.feature.home.domain.model.HomeDashboard
import com.example.m3showcase.feature.home.presentation.components.HomeHeader
import com.example.m3showcase.feature.home.presentation.components.QuickActionGrid
import org.koin.androidx.compose.koinViewModel

/** Navigation callbacks are passed in, so the feature never depends on the navigator. */
@Composable
fun HomeRoute(
    onOpenAccounts: () -> Unit,
    onOpenTransfer: () -> Unit,
    onOpenReceive: () -> Unit,
    onOpenTransactionHistory: (accountId: String?) -> Unit,
    onOpenReceipt: (transactionId: String) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.ShowMessage -> snackbarHostState.showSnackbar(event.text)
                HomeEvent.OpenAccounts -> onOpenAccounts()
                HomeEvent.OpenTransfer -> onOpenTransfer()
                HomeEvent.OpenReceive -> onOpenReceive()
                is HomeEvent.OpenTransactionHistory -> onOpenTransactionHistory(event.accountId)
                is HomeEvent.OpenReceipt -> onOpenReceipt(event.transactionId)
            }
        }
    }
    HomeScreen(state, snackbarHostState, viewModel::onAction)
}

@Composable
fun HomeScreen(
    state: HomeViewState,
    snackbarHostState: SnackbarHostState,
    onAction: (HomeViewAction) -> Unit,
) {
    Scaffold(
        containerColor = Color.Transparent, // AppBackground shows through.
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // The header draws behind the status bar itself.
        contentWindowInsets = WindowInsets(0),
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                HomeViewState.Loading -> LoadingState()
                is HomeViewState.Error -> ErrorState(
                    message = state.message,
                    isOffline = state.isOffline,
                    onRetry = { onAction(HomeViewAction.OnRetry) },
                )
                is HomeViewState.Success -> HomeContent(state.dashboard, state.isRefreshing, onAction)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HomeContent(
    dashboard: HomeDashboard,
    isRefreshing: Boolean,
    onAction: (HomeViewAction) -> Unit,
) {
    val pullState = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { onAction(HomeViewAction.OnRefresh) },
        state = pullState,
        indicator = {
            // Expressive morphing-shape indicator instead of the classic spinner.
            PullToRefreshDefaults.LoadingIndicator(
                state = pullState,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        },
    ) {
        LazyColumn(Modifier.fillMaxSize()) {
            item(key = "header") {
                HomeHeader(
                    user = dashboard.user,
                    accounts = dashboard.accounts,
                    isBalanceHidden = dashboard.isBalanceHidden,
                    onToggleBalance = { onAction(HomeViewAction.OnToggleBalanceVisibility) },
                    onAccountClick = { onAction(HomeViewAction.OnAccountClicked(it)) },
                    onNotificationsClick = { onAction(HomeViewAction.OnQuickActionClicked(QuickAction.Locator)) },
                )
            }
            item(key = "actions") {
                QuickActionGrid(
                    onClick = { onAction(HomeViewAction.OnQuickActionClicked(it)) },
                    modifier = Modifier.padding(16.dp),
                )
            }
            item(key = "recent-title") {
                Row(
                    Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Recent transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { onAction(HomeViewAction.OnSeeAllTransactionsClicked) }) { Text("See all") }
                }
            }
            if (dashboard.recentTransactions.isEmpty()) {
                item(key = "empty") {
                    EmptyState("No transactions yet", "Your latest activity will show up here.")
                }
            } else {
                items(dashboard.recentTransactions, key = { it.id }) { tx ->
                    TransactionItem(
                        transaction = tx,
                        hideAmount = dashboard.isBalanceHidden,
                        onClick = { onAction(HomeViewAction.OnTransactionClicked(tx.id)) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}
