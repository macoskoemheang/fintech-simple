package com.example.m3showcase.feature.loans.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.m3showcase.R
import com.example.m3showcase.core.common.MoneyFormatter
import com.example.m3showcase.core.designsystem.component.CaptionedProgress
import com.example.m3showcase.core.designsystem.component.ServicePalettes
import com.example.m3showcase.core.designsystem.component.StatusPill
import com.example.m3showcase.core.designsystem.component.accountGradient
import com.example.m3showcase.core.ui.formatAccountNumber
import com.example.m3showcase.feature.loans.domain.model.LoanDetail
import com.example.m3showcase.feature.loans.domain.model.LoanType
import kotlin.math.roundToInt

@get:StringRes
internal val LoanType.labelRes: Int
    get() = when (this) {
        LoanType.Personal -> R.string.loan_type_personal
        LoanType.Home -> R.string.loan_type_home
        LoanType.Car -> R.string.loan_type_car
        LoanType.Business -> R.string.loan_type_business
    }

internal val LoanType.icon: ImageVector
    get() = when (this) {
        LoanType.Personal -> Icons.Rounded.Person
        LoanType.Home -> Icons.Rounded.Home
        LoanType.Car -> Icons.Rounded.DirectionsCar
        LoanType.Business -> Icons.Rounded.Business
    }

/** "14 of 36 paid" · "38% repaid", on the dark hero/card. */
@Composable
internal fun RepaymentProgress(detail: LoanDetail) = CaptionedProgress(
    progress = detail.progress,
    start = stringResource(R.string.loan_installments_paid, detail.paidCount, detail.loan.termMonths),
    end = stringResource(R.string.loan_percent_repaid, (detail.progress * 100).roundToInt()),
    onDark = true,
)

@Composable
internal fun LoanCard(detail: LoanDetail, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val loan = detail.loan
    Surface(onClick = onClick, shape = MaterialTheme.shapes.extraLarge, color = Color.Transparent, contentColor = Color.White, modifier = modifier.fillMaxWidth()) {
        Column(Modifier.accountGradient(ServicePalettes.Loan).padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(loan.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(loan.number.formatAccountNumber(), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.74f))
                }
                StatusPill(stringResource(loan.type.labelRes), Color.White.copy(alpha = 0.18f), Color.White)
            }
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.loan_outstanding), style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.74f))
            Text(MoneyFormatter.format(detail.outstanding), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                stringResource(R.string.loan_of_borrowed, MoneyFormatter.format(loan.principal)),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
            )
            Spacer(Modifier.height(16.dp))
            RepaymentProgress(detail)
        }
    }
}
