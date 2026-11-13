package com.example.m3showcase.feature.deposits.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.m3showcase.R
import com.example.m3showcase.core.common.MoneyFormatter
import com.example.m3showcase.core.designsystem.component.CaptionedProgress
import com.example.m3showcase.core.designsystem.component.ServicePalettes
import com.example.m3showcase.core.designsystem.component.StatusPill
import com.example.m3showcase.core.designsystem.component.accountGradient
import com.example.m3showcase.core.ui.dateText
import com.example.m3showcase.core.ui.formatAccountNumber
import com.example.m3showcase.core.ui.formatRate
import com.example.m3showcase.feature.deposits.domain.model.DepositDetail
import com.example.m3showcase.feature.deposits.domain.model.DepositStatus

@Composable
internal fun rateAndTerm(detail: DepositDetail): String = stringResource(
    R.string.deposit_rate_term,
    formatRate(detail.deposit.annualRateBp),
    pluralStringResource(R.plurals.term_months, detail.deposit.termMonths, detail.deposit.termMonths),
)

@Composable
internal fun DepositStatusPill(status: DepositStatus, onDark: Boolean = true) = when (status) {
    DepositStatus.Active -> StatusPill(
        stringResource(R.string.deposit_status_active),
        if (onDark) Color.White.copy(alpha = 0.18f) else MaterialTheme.colorScheme.secondaryContainer,
        if (onDark) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
    )
    DepositStatus.Matured -> StatusPill(stringResource(R.string.deposit_status_matured), Color.White.copy(alpha = 0.92f), ServicePalettes.Deposit.start)
}

/** Progress to maturity with both ends labelled. */
@Composable
internal fun MaturityProgress(detail: DepositDetail) = CaptionedProgress(
    progress = detail.progress,
    start = stringResource(R.string.deposit_matures_on, dateText(detail.maturityDate)),
    end = if (detail.status == DepositStatus.Matured) stringResource(R.string.deposit_status_matured)
    else pluralStringResource(R.plurals.days_left, detail.daysToMaturity.toInt(), detail.daysToMaturity.toInt()),
    onDark = true,
)

/** Gradient card for the deposits list. */
@Composable
internal fun DepositCard(detail: DepositDetail, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(onClick = onClick, shape = MaterialTheme.shapes.extraLarge, color = Color.Transparent, contentColor = Color.White, modifier = modifier.fillMaxWidth()) {
        Column(Modifier.accountGradient(ServicePalettes.Deposit).padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(detail.deposit.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(detail.deposit.number.formatAccountNumber(), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.74f))
                }
                DepositStatusPill(detail.status)
            }
            Spacer(Modifier.height(16.dp))
            Text(MoneyFormatter.format(detail.deposit.principal), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(rateAndTerm(detail), style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
            Spacer(Modifier.height(16.dp))
            MaturityProgress(detail)
        }
    }
}
