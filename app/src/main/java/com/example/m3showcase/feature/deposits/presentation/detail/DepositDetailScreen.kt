package com.example.m3showcase.feature.deposits.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.m3showcase.core.designsystem.component.ServicePalettes
import com.example.m3showcase.core.ui.Loadable
import com.example.m3showcase.core.ui.dateText
import com.example.m3showcase.core.ui.formatAccountNumber
import com.example.m3showcase.core.ui.formatRate
import com.example.m3showcase.feature.deposits.domain.model.DepositDetail
import com.example.m3showcase.feature.deposits.domain.model.InterestPayment
import com.example.m3showcase.feature.deposits.domain.model.InterestPayout
import com.example.m3showcase.feature.deposits.presentation.DepositStatusPill
import com.example.m3showcase.feature.deposits.presentation.MaturityProgress
import com.example.m3showcase.feature.deposits.presentation.rateAndTerm
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DepositDetailRoute(
    depositId: String,
    onBack: () -> Unit,
    viewModel: DepositDetailViewModel = koinViewModel(key = "deposit-$depositId") { parametersOf(depositId) },
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    DepositDetailScreen(state, onBack, viewModel::onRetry)
}

@Composable
fun DepositDetailScreen(state: Loadable<DepositDetail>, onBack: () -> Unit, onRetry: () -> Unit) {
    BankScreenScaffold(title = stringResource(R.string.deposit_detail_title), onBack = onBack) { padding ->
        LoadableContent(state, onRetry, Modifier.padding(padding)) { DepositDetailContent(it) }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DepositDetailContent(detail: DepositDetail) {
    val deposit = detail.deposit
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HeroCard(
            icon = Icons.Rounded.Savings,
            iconShape = MaterialShapes.Cookie6Sided.toShape(),
            label = deposit.name,
            amount = MoneyFormatter.format(deposit.principal),
            subtitle = rateAndTerm(detail),
            palette = ServicePalettes.Deposit,
            badge = { DepositStatusPill(detail.status) },
            bottom = { MaturityProgress(detail) },
        )
        InfoCard(title = stringResource(R.string.deposit_section_interest)) {
            InfoRow(stringResource(R.string.deposit_rate), formatRate(deposit.annualRateBp))
            InfoRow(
                stringResource(R.string.deposit_payout),
                stringResource(if (deposit.payout == InterestPayout.Monthly) R.string.deposit_payout_monthly else R.string.deposit_payout_maturity),
            )
            InfoRow(stringResource(R.string.deposit_interest_to_date), MoneyFormatter.format(detail.interestToDate), valueColor = BankTheme.colors.credit)
            InfoRow(stringResource(R.string.deposit_total_interest), MoneyFormatter.format(detail.totalInterest))
            InfoRow(stringResource(R.string.deposit_maturity_amount), MoneyFormatter.format(detail.maturityAmount), divider = false, strong = true)
        }
        InfoCard(title = stringResource(R.string.deposit_section_term)) {
            InfoRow(stringResource(R.string.deposit_number), deposit.number.formatAccountNumber())
            InfoRow(stringResource(R.string.deposit_start), dateText(deposit.startDate))
            InfoRow(stringResource(R.string.deposit_maturity), dateText(detail.maturityDate))
            InfoRow(stringResource(R.string.deposit_auto_renew), stringResource(if (deposit.autoRenew) R.string.common_yes else R.string.common_no), divider = false)
        }
        InfoCard(title = stringResource(R.string.deposit_schedule)) {
            detail.schedule.forEachIndexed { index, payment ->
                ScheduleRow(payment)
                if (index != detail.schedule.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun ScheduleRow(payment: InterestPayment) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (payment.isPaid) Icons.Rounded.CheckCircle else Icons.Rounded.Schedule,
            contentDescription = stringResource(if (payment.isPaid) R.string.deposit_paid else R.string.deposit_upcoming),
            tint = if (payment.isPaid) BankTheme.colors.credit else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Text("#${payment.number}", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(start = 12.dp).weight(0.25f))
        Text(dateText(payment.date), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(
            MoneyFormatter.format(payment.amount),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (payment.isPaid) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
