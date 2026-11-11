package com.example.m3showcase.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import com.example.m3showcase.core.common.MoneyFormatter
import com.example.m3showcase.core.designsystem.BankTheme
import com.example.m3showcase.core.domain.model.Transaction
import com.example.m3showcase.core.domain.model.TransactionCategory
import com.example.m3showcase.core.ui.DateFormats

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TransactionItem(
    transaction: Transaction,
    modifier: Modifier = Modifier,
    hideAmount: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    ListItem(
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = {
            Surface(
                shape = MaterialShapes.Cookie6Sided.toShape(),
                color = if (transaction.isCredit) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(transaction.category.icon(), null, Modifier.size(22.dp))
                }
            }
        },
        headlineContent = { Text(transaction.title, maxLines = 1) },
        supportingContent = {
            Text(
                "${transaction.counterparty} · ${DateFormats.relativeDay(transaction.timestampMillis)}",
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            Text(
                text = if (hideAmount) MoneyFormatter.masked(transaction.amount.currency)
                else MoneyFormatter.format(transaction.amount, withSign = true),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (transaction.isCredit) BankTheme.colors.credit else BankTheme.colors.debit,
            )
        },
    )
}

fun TransactionCategory.icon(): ImageVector = when (this) {
    TransactionCategory.Transfer -> Icons.Rounded.SwapHoriz
    TransactionCategory.Payment -> Icons.AutoMirrored.Rounded.ReceiptLong
    TransactionCategory.TopUp -> Icons.Rounded.PhoneAndroid
    TransactionCategory.QrPayment -> Icons.Rounded.QrCode2
    TransactionCategory.Salary -> Icons.Rounded.Work
    TransactionCategory.Cash -> Icons.Rounded.Payments
}

