package com.example.m3showcase.core.designsystem

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.luminance
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * App-wide backdrop: the theme's background plus three soft color glows ("aurora")
 * that drift very slowly. Screens draw on top with transparent Scaffolds.
 *
 * - Colors come from the color scheme, so festival themes and dark mode repaint it for free.
 * - The animated value is only read inside drawBehind, so it redraws without recomposing.
 * - Provides onBackground as the default content color for everything inside.
 * - Infinite transitions pause in the background and respect the system
 *   "remove animations" setting, so it costs nothing when nobody is looking.
 */
@Composable
fun AppBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.background.luminance() < 0.5f
    val glowAlpha = if (dark) 0.30f else 0.18f

    val base = scheme.background
    val baseBottom = scheme.surfaceContainerLow
    val glows = listOf(
        scheme.primary.copy(alpha = glowAlpha),
        scheme.tertiary.copy(alpha = glowAlpha * 0.9f),
        scheme.secondary.copy(alpha = glowAlpha * 0.8f),
    )

    val phase = rememberInfiniteTransition(label = "aurora").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 30_000, easing = LinearEasing), RepeatMode.Restart),
        label = "auroraPhase",
    )

    Box(
        modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(Brush.verticalGradient(listOf(base, baseBottom)))
                val t = phase.value * 2f * PI.toFloat()
                // (anchor x, anchor y, radius) as fractions of the screen; each glow orbits its anchor.
                glow(glows[0], 0.15f, 0.12f, 0.85f, t, orbit = 0.06f)
                glow(glows[1], 0.95f, 0.45f, 0.75f, t + 2.1f, orbit = 0.08f)
                glow(glows[2], 0.10f, 0.90f, 0.80f, t + 4.2f, orbit = 0.07f)
            },
    ) {
        // Transparent Scaffolds can't derive a content color from their container
        // (contentColorFor(Transparent) falls back to LocalContentColor), so set it here.
        CompositionLocalProvider(LocalContentColor provides scheme.onBackground) { content() }
    }
}

private fun DrawScope.glow(color: Color, ax: Float, ay: Float, r: Float, t: Float, orbit: Float) {
    val w = size.width
    val center = Offset(
        x = (ax + orbit * cos(t)) * w,
        y = ay * size.height + orbit * sin(t) * w,
    )
    val radius = r * w
    drawCircle(
        brush = Brush.radialGradient(listOf(color, color.copy(alpha = 0f)), center = center, radius = radius),
        radius = radius,
        center = center,
    )
}

/** Top bars over [AppBackground]: see-through at rest, frosted surface once content scrolls under. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bankTopAppBarColors(): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
    containerColor = Color.Transparent,
    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.94f),
)
