package com.example.m3showcase.feature.splash.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.graphics.shapes.Morph
import com.example.m3showcase.core.designsystem.BrandColors
import com.example.m3showcase.core.designsystem.MorphShape
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import com.example.m3showcase.R

/** Size of the mark in the system splash icon (res/drawable/avd_splash_mark.xml). Keep them equal. */
private val MarkSize = 112.dp

/**
 * Branded intro that plays once per cold start, on top of the already-composed first screen.
 *
 * It begins on the exact frame the system splash ends with (same navy, same disc, same "N",
 * same size and position), so the handoff is invisible. Then, in about 1.9 s:
 *  1. a ring pulses out and the disc morphs into the app's Cookie shape (Material 3 Expressive);
 *  2. the mark lifts and the "Nova Bank" wordmark and tagline rise in under it;
 *  3. the whole layer fades and zooms slightly, revealing the screen underneath.
 *
 * Pure presentation: no state beyond the animation, so it needs no ViewModel.
 * It swallows touches while visible so nothing underneath is tapped by accident.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BrandSplash(onFinished: () -> Unit, modifier: Modifier = Modifier) {
    val finished by rememberUpdatedState(onFinished)
    val morph = remember { Morph(MaterialShapes.Circle, MaterialShapes.Cookie9Sided) }

    val ring = remember { Animatable(0f) }   // pulse ring, 0 → 1
    val shape = remember { Animatable(0f) }  // circle → cookie (springs may overshoot)
    val lift = remember { Animatable(0f) }   // mark moves up and shrinks
    val words = remember { Animatable(0f) }  // wordmark + tagline in
    val exit = remember { Animatable(0f) }   // whole layer out

    LaunchedEffect(Unit) {
        launch { ring.animateTo(1f, tween(1_100, easing = FastOutSlowInEasing)) }
        launch { shape.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 140f)) }
        delay(180)
        launch { lift.animateTo(1f, spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow)) }
        delay(220)
        words.animateTo(1f, tween(650, easing = FastOutSlowInEasing))
        delay(500)
        exit.animateTo(1f, tween(420, easing = FastOutLinearInEasing))
        finished()
    }

    val sweep by rememberInfiniteTransition(label = "loader").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1_000, easing = LinearEasing), RepeatMode.Restart),
        label = "sweep",
    )

    Box(
        modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = 1f - exit.value
                val zoom = 1f + 0.08f * exit.value
                scaleX = zoom
                scaleY = zoom
            }
            .background(BrandColors.Navy)
            .drawBehind {
                // Two soft glows that bloom as the mark lifts, echoing the app's aurora background.
                val c = Offset(size.width / 2, size.height / 2)
                val bloom = lift.value.coerceIn(0f, 1f)
                val r1 = size.minDimension * (0.35f + 0.35f * bloom)
                drawCircle(
                    Brush.radialGradient(listOf(BrandColors.Cyan.copy(alpha = 0.28f * bloom), Color.Transparent), c, r1),
                    radius = r1,
                    center = c,
                )
                val c2 = Offset(size.width * 0.15f, size.height * 0.92f)
                val r2 = size.maxDimension * 0.55f
                drawCircle(
                    Brush.radialGradient(listOf(BrandColors.NavyLight.copy(alpha = 0.9f * bloom), Color.Transparent), c2, r2),
                    radius = r2,
                    center = c2,
                )
                // Pulse ring leaving the disc.
                val p = ring.value
                if (p in 0.01f..0.99f) {
                    drawCircle(
                        color = BrandColors.CyanLight.copy(alpha = 0.5f * (1f - p)),
                        radius = MarkSize.toPx() / 2 * (1f + 1.6f * p),
                        center = c,
                        style = Stroke(width = 2.dp.toPx()),
                    )
                }
            }
            // Block touches to the screen underneath while the intro runs.
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
            .clearAndSetSemantics { contentDescription = "Nova Bank" },
        contentAlignment = Alignment.Center,
    ) {
        BrandMark(
            morphProgress = { shape.value },
            modifier = Modifier
                .size(MarkSize)
                .graphicsLayer {
                    translationY = -44.dp.toPx() * lift.value
                    val s = 1f - 0.18f * lift.value
                    scaleX = s
                    scaleY = s
                },
            morph = morph,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.graphicsLayer {
                translationY = 52.dp.toPx() + 18.dp.toPx() * (1f - words.value)
                alpha = words.value
            },
        ) {
            Text(
                "Nova Bank",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                // Letters settle together as they arrive.
                letterSpacing = (1 + 5 * (1f - words.value)).sp,
            )
            Text(
                stringResource(R.string.splash_tagline),
                color = Color.White.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        // A short loading sweep near the bottom.
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 48.dp)
                .graphicsLayer { alpha = words.value }
                .width(72.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.14f))
                .drawBehind {
                    val w = size.width * 0.4f
                    val x = (size.width + w) * sweep - w
                    drawRect(BrandColors.CyanLight, topLeft = Offset(x, 0f), size = size.copy(width = w))
                },
        )
    }
}

/** The cyan mark with a white "N". Same geometry as the vector in avd_splash_mark.xml. */
@Composable
private fun BrandMark(morphProgress: () -> Float, morph: Morph, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val p = morphProgress()
        // The shape turns a little as it morphs; the letter stays upright.
        rotate(degrees = -24f * p) {
            drawOutline(MorphShape(morph, p).createOutline(size, layoutDirection, this), BrandColors.Cyan)
        }
        // "N" in units of the 112-unit mark, centred: same points as the splash vector.
        val u = size.width / 112f
        val cx = size.width / 2
        val cy = size.height / 2
        val points = listOf(-21 to 26, -21 to -26, -9 to -26, 9 to 4, 9 to -26, 21 to -26, 21 to 26, 9 to 26, -9 to -4, -9 to 26)
        val letter = Path().apply {
            points.forEachIndexed { i, (x, y) ->
                if (i == 0) moveTo(cx + x * u, cy + y * u) else lineTo(cx + x * u, cy + y * u)
            }
            close()
        }
        drawPath(letter, Color.White)
    }
}
