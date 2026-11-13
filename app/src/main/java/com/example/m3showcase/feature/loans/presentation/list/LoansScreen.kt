package com.example.m3showcase.feature.loans.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.example.m3showcase.core.ui.asString
import com.example.m3showcase.core.ui.dateText
import com.example.m3showcase.feature.loans.presentation.LoanCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoansRoute(
    onBack: () -> Unit,
    onOpenLoan: (String) -> Unit,
    onOpenReport: () -> Unit,
    viewModel: LoansViewModel = koinViewModel(),
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is LoansEvent.ShowMessage -> snackbarHostState.showSnackbar(event.text.asString(context))
                is LoansEvent.OpenLoan -> onOpenLoan(event.id)
                LoansEvent.OpenReport -> onOpenReport()
                LoansEvent.NavigateBack -> onBack()
            }
        }
    }
    LoansScreen(state, snackbarHostState, viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoansScreen(state: LoansViewState, snackbarHostState: SnackbarHostState, onAction: (LoansViewAction) -> Unit) {
    BankScreenScaffold(
        title = stringResource(R.string.loans_title),
        onBack = { onAction(LoansViewAction.OnBackClicked) },
        snackbarHostState = snackbarHostState,
        actions = {
            IconButton(onClick = { onAction(LoansViewAction.OnReportClicked) }) {
                Icon(Icons.Rounded.Assessment, contentDescription = stringResource(R.string.loan_report_title))
            }
        },
    ) { padding ->
        LoadableContent(state.content, onRetry = { onAction(LoansViewAction.OnRetry) }, modifier = Modifier.padding(padding)) { portfolio ->
            PullToRefreshBox(isRefreshing = state.isRefreshing, onRefresh = { onAction(LoansViewAction.OnRefresh) }) {
                if (portfolio.loans.isEmpty()) {
                    EmptyState(stringResource(R.string.loans_empty_title), stringResource(R.string.loans_empty_message), icon = Icons.Outlined.AccountBalance)
                    return@PullToRefreshBox
                }
                LazyColumn(Modifier.fillMaxSize(), contentPadding = ScreenContentPadding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item(key = "summary") {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SummaryTile(
                                stringResource(R.string.loans_total_outstanding),
                                CurrencyCode.entries.mapNotNull { portfolio.outstandingByCurrency[it]?.let(MoneyFormatter::format) }.joinToString("\n"),
                                Modifier.weight(1f),
                            )
                            SummaryTile(
                                stringResource(R.string.loans_next_payment),
                                portfolio.nextPayment?.let { (_, inst) -> "${MoneyFormatter.format(inst.payment)}\n${dateText(inst.dueDate)}" } ?: "–",
                                Modifier.weight(1f),
                            )
                        }
                    }
                    item(key = "report") { ReportEntry { onAction(LoansViewAction.OnReportClicked) } }
                    items(portfolio.loans, key = { it.loan.id }) { detail ->
                        LoanCard(detail, { onAction(LoansViewAction.OnLoanClicked(detail.loan.id)) }, Modifier.animateItem())
                    }
                }
            }
        }
    }
}

/** Entry to the loan (credit-bureau trigger) report. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ReportEntry(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = MaterialShapes.Clover4Leaf.toShape(), color = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Assessment, null) }
            }
            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text(stringResource(R.string.loan_report_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.loan_report_entry_desc), style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null)
        }
    }
}
