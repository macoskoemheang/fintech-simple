package com.example.m3showcase.core.designsystem

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.example.m3showcase.core.domain.model.EventTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * Festival artwork drawn behind the navy (or festival-colored) hero.
 * Kept faint and in the corners so balances and names stay readable on top.
 * Pure Canvas: no image assets, scales to any hero size.
 *
 * @param animate false for small static previews (the theme picker), so a grid of
 * cards doesn't run several infinite animations at once.
 */
@Composable
fun FestivalHeroArt(
    theme: EventTheme = BankTheme.eventTheme,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
) {
    if (!theme.isFestival) return
    val accent = BankTheme.colors.heroAccent
    val t = festivalClock(animate)
    // Decoration only: hide it from screen readers.
    Canvas(modifier.clearAndSetSemantics {}) {
        val progress = t.value
        when (theme) {
            EventTheme.WaterFestival -> drawWaterFestival(progress, accent)
            EventTheme.KhmerNewYear -> drawKhmerNewYear(progress, accent)
            EventTheme.PchumBen -> drawPchumBen(progress, accent)
            EventTheme.IndependenceDay -> drawIndependenceDay(progress, accent)
            EventTheme.Auto, EventTheme.Default -> Unit
        }
    }
}

/** 0→1 over 6 s, looping. Reading it inside Canvas means only the draw phase reruns. */
@Composable
private fun festivalClock(animate: Boolean): State<Float> {
    if (!animate) return remember { mutableFloatStateOf(0.3f) }
    return rememberInfiniteTransition(label = "festival").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(6_000, easing = LinearEasing), RepeatMode.Restart),
        label = "festivalClock",
    )
}

private const val TAU = (2 * PI).toFloat()

// --- Water Festival: full moon, fireworks, waves and a racing long-boat -----------------

private fun DrawScope.drawWaterFestival(t: Float, gold: Color) {
    val unit = min(size.width, size.height)
    // Full moon: Bon Om Touk ends with Ak Ambok, the salute to the moon.
    val moon = Offset(size.width * 0.86f, size.height * 0.16f)
    drawCircle(Brush.radialGradient(listOf(gold.copy(alpha = 0.35f), Color.Transparent), moon, unit * 0.22f), unit * 0.22f, moon)
    drawCircle(gold.copy(alpha = 0.55f), unit * 0.07f, moon)

    drawFirework(Offset(size.width * 0.66f, size.height * 0.12f), unit * 0.10f, phase(t, 0f), gold)
    drawFirework(Offset(size.width * 0.95f, size.height * 0.42f), unit * 0.07f, phase(t, 0.45f), Color(0xFF7FE3F5))

    // Two wave layers drifting at different speeds.
    drawWave(baseline = size.height * 0.90f, amplitude = unit * 0.025f, shift = t * TAU, color = Color.White.copy(alpha = 0.07f))
    drawWave(baseline = size.height * 0.95f, amplitude = unit * 0.018f, shift = -t * TAU * 2, color = Color.White.copy(alpha = 0.10f))

    // Long-boat silhouette with rowers, bobbing on the water.
    val bob = sin(t * TAU) * unit * 0.008f
    val boatW = size.width * 0.34f
    val left = size.width * 0.62f
    val waterline = size.height * 0.90f + bob
    val hull = Path().apply {
        moveTo(left, waterline - unit * 0.035f) // stern, raised
        quadraticTo(left + boatW * 0.5f, waterline + unit * 0.02f, left + boatW, waterline - unit * 0.02f)
        // Prow curling up like a naga head.
        quadraticTo(left + boatW * 1.06f, waterline - unit * 0.07f, left + boatW * 0.98f, waterline - unit * 0.09f)
        quadraticTo(left + boatW * 0.97f, waterline - unit * 0.03f, left + boatW * 0.9f, waterline - unit * 0.01f)
        lineTo(left + boatW * 0.1f, waterline - unit * 0.01f)
        close()
    }
    drawPath(hull, gold.copy(alpha = 0.45f))
    val rowers = 8
    repeat(rowers) { i ->
        val x = left + boatW * (0.16f + i * 0.09f)
        val head = Offset(x, waterline - unit * 0.04f)
        drawCircle(Color.White.copy(alpha = 0.35f), unit * 0.009f, head)
        // Oars sweep in time.
        val stroke = sin(t * TAU * 3) * unit * 0.015f
        drawLine(
            Color.White.copy(alpha = 0.25f),
            start = Offset(x, waterline - unit * 0.02f),
            end = Offset(x - unit * 0.02f + stroke, waterline + unit * 0.025f),
            strokeWidth = unit * 0.005f,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawWave(baseline: Float, amplitude: Float, shift: Float, color: Color) {
    val path = Path().apply {
        moveTo(0f, size.height)
        lineTo(0f, baseline)
        val steps = 32
        for (i in 0..steps) {
            val x = size.width * i / steps
            lineTo(x, baseline + sin(x / size.width * TAU * 2 + shift) * amplitude)
        }
        lineTo(size.width, size.height)
        close()
    }
    drawPath(path, color)
}

// --- Khmer New Year: slow sun rays and falling flower petals -----------------------------

private fun DrawScope.drawKhmerNewYear(t: Float, gold: Color) {
    val unit = min(size.width, size.height)
    val sun = Offset(size.width * 0.92f, size.height * 0.08f)
    rotate(degrees = t * 30f, pivot = sun) {
        repeat(12) { i ->
            val angle = i * TAU / 12
            drawLine(
                gold.copy(alpha = 0.16f),
                start = sun + polar(angle, unit * 0.12f),
                end = sun + polar(angle, unit * 0.55f),
                strokeWidth = unit * 0.05f,
                cap = StrokeCap.Round,
            )
        }
    }
    drawCircle(gold.copy(alpha = 0.45f), unit * 0.08f, sun)

    // Petals drift down and loop; positions are seeded so they don't jump between frames.
    val random = Random(13)
    repeat(14) {
        val x = random.nextFloat() * size.width
        val speed = 0.6f + random.nextFloat() * 0.6f
        val y = ((random.nextFloat() + t * speed) % 1f) * size.height
        val color = if (it % 3 == 0) Color.White else gold
        drawFlower(Offset(x, y), unit * (0.012f + random.nextFloat() * 0.012f), color.copy(alpha = 0.35f), spin = t * 360f + it * 40f)
    }
}

/** Five-petal blossom. */
private fun DrawScope.drawFlower(center: Offset, radius: Float, color: Color, spin: Float) {
    rotate(spin, center) {
        repeat(5) { i ->
            drawCircle(color, radius * 0.6f, center + polar(i * TAU / 5, radius * 0.7f))
        }
    }
    drawCircle(Color.White.copy(alpha = 0.5f), radius * 0.3f, center)
}

// --- Pchum Ben: lotus and floating candle lights ----------------------------------------

private fun DrawScope.drawPchumBen(t: Float, saffron: Color) {
    val unit = min(size.width, size.height)
    val random = Random(7)
    repeat(9) {
        val x = size.width * (0.45f + random.nextFloat() * 0.55f)
        val y = ((random.nextFloat() - t * (0.15f + random.nextFloat() * 0.15f)) % 1f + 1f) % 1f * size.height
        val glow = 0.25f + 0.2f * sin((t + it * 0.13f) * TAU)
        val r = unit * (0.03f + random.nextFloat() * 0.03f)
        drawCircle(Brush.radialGradient(listOf(saffron.copy(alpha = glow), Color.Transparent), Offset(x, y), r), r, Offset(x, y))
    }
    drawLotus(Offset(size.width * 0.84f, size.height * 0.97f), unit * 0.32f, saffron)
}

private fun DrawScope.drawLotus(base: Offset, height: Float, color: Color) {
    // Back row then front row; each petal is a pointed leaf shape fanned from the base.
    listOf(-50f, -25f, 0f, 25f, 50f).forEachIndexed { i, angle ->
        val alpha = if (i % 2 == 0) 0.22f else 0.32f
        rotate(angle, base) {
            val petal = Path().apply {
                moveTo(base.x, base.y)
                quadraticTo(base.x - height * 0.28f, base.y - height * 0.5f, base.x, base.y - height)
                quadraticTo(base.x + height * 0.28f, base.y - height * 0.5f, base.x, base.y)
                close()
            }
            drawPath(petal, color.copy(alpha = alpha))
        }
    }
}

// --- Independence Day: fireworks in flag colors -----------------------------------------

private fun DrawScope.drawIndependenceDay(t: Float, red: Color) {
    val unit = min(size.width, size.height)
    drawFirework(Offset(size.width * 0.82f, size.height * 0.18f), unit * 0.14f, phase(t, 0f), Color.White)
    drawFirework(Offset(size.width * 0.62f, size.height * 0.08f), unit * 0.08f, phase(t, 0.33f), red)
    drawFirework(Offset(size.width * 0.95f, size.height * 0.55f), unit * 0.10f, phase(t, 0.66f), Color(0xFF9FB4FF))
    // A few steady sparkles.
    val random = Random(3)
    repeat(10) {
        val p = Offset(size.width * (0.5f + random.nextFloat() * 0.5f), size.height * random.nextFloat())
        val twinkle = 0.15f + 0.25f * ((sin((t * 2 + it * 0.21f) * TAU) + 1f) / 2f)
        drawCircle(Color.White.copy(alpha = twinkle), unit * 0.006f, p)
    }
}

// --- Shared helpers ---------------------------------------------------------------------

/** A burst that expands and fades. [p] is 0→1 through one burst. */
private fun DrawScope.drawFirework(center: Offset, radius: Float, p: Float, color: Color) {
    val reach = radius * (0.3f + 0.7f * p)
    val alpha = (1f - p) * 0.7f
    repeat(12) { i ->
        val angle = i * TAU / 12
        drawLine(
            color.copy(alpha = alpha),
            start = center + polar(angle, reach * 0.45f),
            end = center + polar(angle, reach),
            strokeWidth = radius * 0.06f,
            cap = StrokeCap.Round,
        )
        drawCircle(color.copy(alpha = alpha), radius * 0.04f, center + polar(angle, reach * 1.12f))
    }
}

private fun phase(t: Float, offset: Float): Float = (t * 2 + offset) % 1f

private fun polar(angle: Float, r: Float) = Offset(cos(angle) * r, sin(angle) * r)
