package com.example.m3showcase.feature.accounts.presentation.list

import com.example.m3showcase.core.designsystem.bankTopAppBarColors
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import com.example.m3showcase.core.designsystem.component.AccountCard
import com.example.m3showcase.core.designsystem.component.cardPalette
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.res.stringResource
import com.example.m3showcase.R
import com.example.m3showcase.core.ui.asString
import androidx.compose.ui.platform.LocalContext

@Composable
fun AccountsRoute(
    onOpenHistory: (accountId: String) -> Unit,
    onOpenTransfer: (accountId: String) -> Unit,
    viewModel: AccountsViewModel = koinViewModel(),
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is AccountsEvent.ShowMessage -> snackbarHostState.showSnackbar(event.text.asString(context))
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
        containerColor = Color.Transparent, // AppBackground shows through.
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                colors = bankTopAppBarColors(),
                title = { Text(stringResource(R.string.accounts_title)) },
                subtitle = { Text(stringResource(R.string.accounts_subtitle)) },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                AccountsViewState.Loading -> LoadingState()
                AccountsViewState.Empty -> EmptyState(stringResource(R.string.accounts_empty_title), stringResource(R.string.accounts_empty_message))
                is AccountsViewState.Error -> ErrorState(state.message.asString(), { onAction(AccountsViewAction.OnRefresh) }, isOffline = state.isOffline)
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
                            AccountRow(account, state.isBalanceHidden, onAction, Modifier.animateItem())
                        }
                    }
                }
            }
        }
    }
}

/** One account as a bank card, with Transfer and History actions on it. Tapping the card also opens history. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AccountRow(account: Account, hidden: Boolean, onAction: (AccountsViewAction) -> Unit, modifier: Modifier = Modifier) {
    val open = { onAction(AccountsViewAction.OnAccountClicked(account.id)) }
    AccountCard(
        account = account,
        balanceText = if (hidden) MoneyFormatter.masked(account.balance.currency) else MoneyFormatter.format(account.balance),
        onClick = open,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { onAction(AccountsViewAction.OnTransferClicked(account.id)) },
                shapes = ButtonDefaults.shapes(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = account.type.cardPalette().start),
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Rounded.SwapHoriz, null, Modifier.size(18.dp))
                Text(stringResource(R.string.accounts_transfer), Modifier.padding(start = 6.dp), fontWeight = FontWeight.SemiBold)
            }
            OutlinedButton(
                onClick = open,
                shapes = ButtonDefaults.shapes(),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.AutoMirrored.Rounded.ReceiptLong, null, Modifier.size(18.dp))
                Text(stringResource(R.string.accounts_history), Modifier.padding(start = 6.dp), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
