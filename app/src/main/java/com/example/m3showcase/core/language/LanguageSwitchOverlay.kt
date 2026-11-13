package com.example.m3showcase.core.language

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.m3showcase.R
import com.example.m3showcase.core.designsystem.BrandColors
import kotlinx.coroutines.delay
import kotlin.math.hypot
import androidx.compose.ui.geometry.Rect

/** Time the loader stays up after covering, so the change reads as a deliberate step. */
private const val HoldBeforeApplyMs = 420L

/** Time to wait for the recreated Activity's first frame before revealing it. */
private const val HoldBeforeRevealMs = 480L

/**
 * Full-screen transition for a language change. Put it on top of everything in the Activity.
 *
 * 1. Covering: a navy circle grows from the centre of the screen; then the target language's
 *    name, a "Changing language…" line already written in that language, and the Expressive
 *    morphing loader fade in.
 * 2. Applying: the language is set and the Activity recreates *under* the overlay. The new
 *    Activity composes this overlay already fully covering, so the recreation is never seen.
 * 3. The overlay fades and zooms slightly away, revealing the app in the new language.
 *
 * Touches are swallowed for the whole transition.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LanguageSwitchOverlay(controller: LanguageSwitchController) {
    val phase by controller.phase.collectAsStateWithLifecycle()
    val labels = phase.labels ?: return
    val haptics = LocalHapticFeedback.current

    // A fresh Activity in the Applying phase starts fully covered: that's what hides the recreation.
    val reveal = remember { Animatable(if (phase is LanguageSwitchPhase.Applying) 1f else 0f) }
    val exit = remember { Animatable(0f) }

    LaunchedEffect(phase) {
        when (phase) {
            is LanguageSwitchPhase.Covering -> {
                reveal.animateTo(1f, tween(560, easing = FastOutSlowInEasing))
                delay(HoldBeforeApplyMs)
                controller.onCovered()
            }
            is LanguageSwitchPhase.Applying -> {
                // If the language resolves to the one already showing, nothing recreates and this
                // same effect reveals; otherwise it is cancelled and the new Activity runs it.
                reveal.snapTo(1f)
                delay(HoldBeforeRevealMs)
                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                exit.animateTo(1f, tween(380, easing = FastOutLinearInEasing))
                controller.onRevealed()
            }
            LanguageSwitchPhase.Idle -> Unit
        }
    }

    val message = stringResourceIn(labels.targetLocale, R.string.language_switching)

    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = 1f - exit.value
                val zoom = 1f + 0.06f * exit.value
                scaleX = zoom
                scaleY = zoom
            }
            .drawWithContent {
                // Circular reveal from the centre.
                val center = Offset(size.width / 2, size.height / 2)
                val radius = hypot(size.width, size.height) / 2 * reveal.value
                val circle = Path().apply { addOval(Rect(center, radius)) }
                clipPath(circle) { this@drawWithContent.drawContent() }
            }
            .background(Brush.verticalGradient(listOf(BrandColors.NavyDeep, BrandColors.Navy, BrandColors.NavyLight)))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
            .clearAndSetSemantics {
                contentDescription = message
                liveRegion = LiveRegionMode.Polite
            },
        contentAlignment = Alignment.Center,
    ) {
        // Content arrives once the circle has mostly covered the screen.
        val contentAlpha = ((reveal.value - 0.55f) / 0.45f).coerceIn(0f, 1f)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(32.dp)
                .graphicsLayer {
                    alpha = contentAlpha
                    translationY = 24.dp.toPx() * (1f - contentAlpha)
                },
        ) {
            Surface(
                shape = MaterialShapes.Cookie9Sided.toShape(),
                color = BrandColors.Cyan,
                contentColor = Color.White,
                modifier = Modifier.size(72.dp),
            ) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Translate, null, Modifier.size(34.dp)) }
            }
            Spacer(Modifier.height(28.dp))
            FromToRow(from = labels.fromName, to = labels.targetName)
            Spacer(Modifier.height(10.dp))
            Text(
                labels.targetName,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(36.dp))
            LoadingIndicator(color = BrandColors.CyanLight, modifier = Modifier.size(56.dp))
        }
    }
}

/** "English → ខ្មែរ", small, above the big name: shows what is changing to what. */
@Composable
private fun FromToRow(from: String, to: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(from, style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.55f))
        Icon(
            Icons.AutoMirrored.Rounded.ArrowForward,
            contentDescription = null,
            tint = BrandColors.CyanLight,
            modifier = Modifier.size(16.dp),
        )
        Text(to, style = MaterialTheme.typography.labelLarge, color = BrandColors.CyanLight)
    }
}
