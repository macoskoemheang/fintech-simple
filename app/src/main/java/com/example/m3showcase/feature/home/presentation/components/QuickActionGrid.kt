package com.example.m3showcase.feature.home.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.RequestQuote
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.m3showcase.feature.home.presentation.QuickAction
import androidx.compose.ui.res.stringResource

/** ABA-style services grid: 4 per row, each icon in its own Expressive shape. */
@Composable
fun QuickActionGrid(onClick: (QuickAction) -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        Column(Modifier.padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            QuickAction.entries.chunked(4).forEach { row ->
                Row(Modifier.fillMaxWidth()) {
                    row.forEach { action ->
                        QuickActionTile(action, Modifier.weight(1f)) { onClick(action) }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionTile(action: QuickAction, modifier: Modifier, onClick: () -> Unit) {
    val ui = action.ui()
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            shape = ui.shape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(56.dp),
        ) {
            Box(contentAlignment = Alignment.Center) { Icon(ui.icon, contentDescription = null) }
        }
        Text(
            ui.label,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

private data class QuickActionUi(val label: String, val icon: ImageVector, val shape: Shape)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun QuickAction.ui(): QuickActionUi = when (this) {
    QuickAction.Accounts -> QuickActionUi(stringResource(labelRes), Icons.Rounded.AccountBalanceWallet, MaterialShapes.Cookie9Sided.toShape())
    QuickAction.Transfer -> QuickActionUi(stringResource(labelRes), Icons.Rounded.SwapHoriz, MaterialShapes.Sunny.toShape())
    QuickAction.Receive -> QuickActionUi(stringResource(labelRes), Icons.Rounded.QrCode2, MaterialShapes.Square.toShape())
    QuickAction.Payments -> QuickActionUi(stringResource(labelRes), Icons.Rounded.RequestQuote, MaterialShapes.Clover4Leaf.toShape())
    QuickAction.Cards -> QuickActionUi(stringResource(labelRes), Icons.Rounded.CreditCard, MaterialShapes.Pill.toShape())
    QuickAction.Deposits -> QuickActionUi(stringResource(labelRes), Icons.Rounded.Savings, MaterialShapes.Cookie6Sided.toShape())
    QuickAction.Loans -> QuickActionUi(stringResource(labelRes), Icons.Rounded.AccountBalance, MaterialShapes.Gem.toShape())
    QuickAction.Locator -> QuickActionUi(stringResource(labelRes), Icons.Rounded.LocationOn, MaterialShapes.Puffy.toShape())
}
