package com.example.m3showcase.feature.deposits.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.m3showcase.R
import com.example.m3showcase.core.common.MoneyFormatter
import com.example.m3showcase.core.designsystem.component.BankScreenScaffold
import com.example.m3showcase.core.designsystem.component.EmptyState
import com.example.m3showcase.core.designsystem.component.LoadableContent
import com.example.m3showcase.core.designsystem.component.ScreenContentPadding
import com.example.m3showcase.core.designsystem.component.SummaryTile
import com.example.m3showcase.core.domain.model.CurrencyCode
import com.example.m3showcase.core.domain.model.Money
import com.example.m3showcase.core.ui.asString
import com.example.m3showcase.feature.deposits.presentation.DepositCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun DepositsRoute(onBack: () -> Unit, onOpenDeposit: (String) -> Unit, viewModel: DepositsViewModel = koinViewModel()) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is DepositsEvent.ShowMessage -> snackbarHostState.showSnackbar(event.text.asString(context))
                is DepositsEvent.OpenDeposit -> onOpenDeposit(event.id)
                DepositsEvent.NavigateBack -> onBack()
            }
        }
    }
    DepositsScreen(state, snackbarHostState, viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositsScreen(state: DepositsViewState, snackbarHostState: SnackbarHostState, onAction: (DepositsViewAction) -> Unit) {
    BankScreenScaffold(
        title = stringResource(R.string.deposits_title),
        onBack = { onAction(DepositsViewAction.OnBackClicked) },
        snackbarHostState = snackbarHostState,
    ) { padding ->
        LoadableContent(state.content, onRetry = { onAction(DepositsViewAction.OnRetry) }, modifier = Modifier.padding(padding)) { portfolio ->
            PullToRefreshBox(isRefreshing = state.isRefreshing, onRefresh = { onAction(DepositsViewAction.OnRefresh) }) {
                if (portfolio.deposits.isEmpty()) {
                    EmptyState(stringResource(R.string.deposits_empty_title), stringResource(R.string.deposits_empty_message), icon = Icons.Outlined.Savings)
                    return@PullToRefreshBox
                }
                LazyColumn(Modifier.fillMaxSize(), contentPadding = ScreenContentPadding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item(key = "summary") {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SummaryTile(stringResource(R.string.deposits_total), portfolio.principalByCurrency.lines(), Modifier.weight(1f))
                            SummaryTile(stringResource(R.string.deposits_interest_earned), portfolio.interestToDateByCurrency.lines(), Modifier.weight(1f))
                        }
                    }
                    items(portfolio.deposits, key = { it.deposit.id }) { detail ->
                        DepositCard(detail, { onAction(DepositsViewAction.OnDepositClicked(detail.deposit.id)) }, Modifier.animateItem())
                    }
                }
            }
        }
    }
}

/** One line per currency, USD first. */
private fun Map<CurrencyCode, Money>.lines(): String =
    CurrencyCode.entries.mapNotNull { this[it]?.let(MoneyFormatter::format) }.joinToString("\n").ifEmpty { "–" }
