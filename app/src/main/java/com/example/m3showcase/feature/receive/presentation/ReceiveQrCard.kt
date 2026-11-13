package com.example.m3showcase.feature.receive.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.m3showcase.R
import com.example.m3showcase.core.designsystem.BrandColors
import com.example.m3showcase.core.designsystem.component.QrCode
import com.example.m3showcase.core.domain.model.Money
import com.example.m3showcase.core.common.MoneyFormatter
import com.example.m3showcase.core.domain.model.ReceiveQr
import com.example.m3showcase.core.ui.formatAccountNumber

/**
 * The white "scan to pay" card. It stays white in dark mode too, so scanners can read it.
 * [amount] / [note] appear under the name when the QR asks for a fixed amount.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ReceiveQrCard(
    qr: ReceiveQr,
    payload: String,
    modifier: Modifier = Modifier,
    qrSize: Dp = 220.dp,
    amount: Money? = null,
    note: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val content: @Composable () -> Unit = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(BrandColors.Coral, RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.receive_scan_to_pay), color = Color.White, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
            }
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(qr.accountName, color = Color(0xFF181C20), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("${qr.accountNumber.formatAccountNumber()} · ${qr.currency.name}", color = Color(0xFF41484F), style = MaterialTheme.typography.bodyMedium)
                AnimatedVisibility(amount != null, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 10.dp)) {
                        Text(
                            amount?.let(MoneyFormatter::format).orEmpty(),
                            color = BrandColors.NavyDeep,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        note?.let { Text(it, color = Color(0xFF41484F), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center) }
                    }
                }
                Spacer(Modifier.height(20.dp))
                QrCode(content = payload, size = qrSize, color = BrandColors.NavyDeep) {
                    Surface(
                        shape = MaterialShapes.Cookie9Sided.toShape(),
                        color = BrandColors.Cyan,
                        border = BorderStroke(4.dp, Color.White),
                        modifier = Modifier.size(48.dp),
                    ) { Box(contentAlignment = Alignment.Center) { Text("N", color = Color.White, fontWeight = FontWeight.Black) } }
                }
            }
        }
    }
    if (onClick != null) {
        Surface(onClick = onClick, shape = MaterialTheme.shapes.extraLarge, color = Color.White, shadowElevation = 6.dp, modifier = modifier.fillMaxWidth()) { content() }
    } else {
        Surface(shape = MaterialTheme.shapes.extraLarge, color = Color.White, shadowElevation = 6.dp, modifier = modifier.fillMaxWidth()) { content() }
    }
}
