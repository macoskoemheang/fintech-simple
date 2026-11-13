package com.example.m3showcase.feature.loans.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.m3showcase.R
import com.example.m3showcase.core.common.MoneyFormatter
import com.example.m3showcase.core.designsystem.BankTheme
import com.example.m3showcase.core.designsystem.component.BankScreenScaffold
import com.example.m3showcase.core.designsystem.component.HeroCard
import com.example.m3showcase.core.designsystem.component.InfoCard
import com.example.m3showcase.core.designsystem.component.InfoRow
import com.example.m3showcase.core.designsystem.component.LoadableContent
import com.example.m3showcase.core.designsystem.component.ScreenContentPadding
import com.example.m3showcase.core.designsystem.component.ServicePalettes
import com.example.m3showcase.core.designsystem.component.StatusPill
import com.example.m3showcase.core.ui.Loadable
import com.example.m3showcase.core.ui.dateText
import com.example.m3showcase.core.ui.formatAccountNumber
import com.example.m3showcase.core.ui.formatRate
import com.example.m3showcase.feature.loans.domain.model.Installment
import com.example.m3showcase.feature.loans.domain.model.InstallmentStatus
import com.example.m3showcase.feature.loans.domain.model.LoanDetail
import com.example.m3showcase.feature.loans.presentation.RepaymentProgress
import com.example.m3showcase.feature.loans.presentation.icon
import com.example.m3showcase.feature.loans.presentation.labelRes
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun LoanDetailRoute(
    loanId: String,
    onBack: () -> Unit,
    viewModel: LoanDetailViewModel = koinViewModel(key = "loan-$loanId") { parametersOf(loanId) },
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    LoanDetailScreen(state, onBack, viewModel::onRetry)
}

@Composable
fun LoanDetailScreen(state: Loadable<LoanDetail>, onBack: () -> Unit, onRetry: () -> Unit) {
    BankScreenScaffold(title = stringResource(R.string.loan_detail_title), onBack = onBack) { padding ->
        LoadableContent(state, onRetry, Modifier.padding(padding)) { LoanDetailContent(it) }
    }
}

/** Lazy, because a 60-month schedule is long. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoanDetailContent(detail: LoanDetail) {
    val loan = detail.loan
    LazyColumn(Modifier.fillMaxSize(), contentPadding = ScreenContentPadding, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item(key = "hero") {
            HeroCard(
                icon = loan.type.icon,
                iconShape = MaterialShapes.Gem.toShape(),
                label = stringResource(R.string.loan_outstanding),
                amount = MoneyFormatter.format(detail.outstanding),
                subtitle = stringResource(R.string.loan_of_borrowed, MoneyFormatter.format(loan.principal)),
                palette = ServicePalettes.Loan,
                badge = { StatusPill(stringResource(loan.type.labelRes), Color.White.copy(alpha = 0.18f), Color.White) },
                bottom = { RepaymentProgress(detail) },
            )
        }
        detail.nextInstallment?.let { next -> item(key = "next") { NextPaymentCard(next) } }
            ?: item(key = "paid-off") {
                Surface(shape = MaterialTheme.shapes.large, color = BankTheme.colors.credit.copy(alpha = 0.16f), modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.loan_paid_off), Modifier.padding(16.dp), color = BankTheme.colors.credit, fontWeight = FontWeight.SemiBold)
                }
            }
        item(key = "info") {
            InfoCard(title = stringResource(R.string.loan_section_details)) {
                InfoRow(stringResource(R.string.loan_name), loan.name)
                InfoRow(stringResource(R.string.loan_number), loan.number.formatAccountNumber())
                InfoRow(stringResource(R.string.loan_principal), MoneyFormatter.format(loan.principal))
                InfoRow(stringResource(R.string.deposit_rate), formatRate(loan.annualRateBp))
                InfoRow(stringResource(R.string.loan_term), pluralStringResource(R.plurals.term_months, loan.termMonths, loan.termMonths))
                InfoRow(stringResource(R.string.loan_monthly_payment), MoneyFormatter.format(detail.monthlyPayment), strong = true)
                InfoRow(stringResource(R.string.loan_total_interest), MoneyFormatter.format(detail.totalInterest))
                InfoRow(stringResource(R.string.loan_disbursed), dateText(loan.startDate))
                InfoRow(stringResource(R.string.loan_end_date), dateText(detail.endDate), divider = false)
            }
        }
        item(key = "schedule-title") {
            Text(
                stringResource(R.string.loan_schedule),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
        items(detail.schedule, key = { "inst-${it.number}" }) { InstallmentRow(it) }
    }
}

@Composable
private fun NextPaymentCard(next: Installment) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Event, null)
            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text(stringResource(R.string.loans_next_payment), style = MaterialTheme.typography.labelLarge)
                Text(stringResource(R.string.loan_due_on, dateText(next.dueDate)), style = MaterialTheme.typography.bodyMedium)
            }
            Text(MoneyFormatter.format(next.payment), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun InstallmentRow(installment: Installment) {
    val isNext = installment.status == InstallmentStatus.Due
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (isNext) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
        else MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.94f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (installment.status == InstallmentStatus.Paid) Icons.Rounded.CheckCircle else Icons.Rounded.Schedule,
                contentDescription = stringResource(
                    when (installment.status) {
                        InstallmentStatus.Paid -> R.string.deposit_paid
                        InstallmentStatus.Due -> R.string.loan_status_due
                        InstallmentStatus.Upcoming -> R.string.deposit_upcoming
                    },
                ),
                tint = if (installment.status == InstallmentStatus.Paid) BankTheme.colors.credit else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text("#${installment.number} · ${dateText(installment.dueDate)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(
                    stringResource(
                        R.string.loan_installment_breakdown,
                        MoneyFormatter.format(installment.principalPart),
                        MoneyFormatter.format(installment.interestPart),
                        MoneyFormatter.format(installment.balanceAfter),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(MoneyFormatter.format(installment.payment), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
    }
}
