package com.example.m3showcase.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Draws a scannable QR code. Encoding runs off the main thread. */
@Composable
fun QrCode(
    content: String,
    size: Dp,
    modifier: Modifier = Modifier,
    color: Color = Color.Black,
    centerContent: (@Composable () -> Unit)? = null,
) {
    val matrix by produceState<BitMatrix?>(initialValue = null, content) {
        value = withContext(Dispatchers.Default) {
            QRCodeWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                0,
                0,
                mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H, EncodeHintType.MARGIN to 0),
            )
        }
    }
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        matrix?.let { m ->
            Canvas(Modifier.size(size)) {
                val cell = this.size.width / m.width
                for (y in 0 until m.height) for (x in 0 until m.width) {
                    if (m[x, y]) drawRect(color, Offset(x * cell, y * cell), Size(cell + 0.5f, cell + 0.5f))
                }
            }
            centerContent?.invoke()
        }
    }
}
