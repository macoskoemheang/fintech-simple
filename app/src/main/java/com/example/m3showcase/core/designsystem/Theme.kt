package com.example.m3showcase.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.m3showcase.core.domain.model.EventTheme
import com.example.m3showcase.core.domain.model.ThemeMode

/** Brand colors that Material's ColorScheme has no slot for. */
@Immutable
data class BankExtendedColors(
    val heroBrush: Brush,
    val onHero: Color,
    val onHeroVariant: Color,
    val heroAccent: Color,
    val credit: Color,
    val debit: Color,
)

private val LightExtended = BankExtendedColors(
    heroBrush = Brush.verticalGradient(listOf(BrandColors.Navy, BrandColors.NavyLight)),
    onHero = Color.White,
    onHeroVariant = Color.White.copy(alpha = 0.72f),
    heroAccent = BrandColors.CyanLight,
    credit = Color(0xFF1B8A4F),
    debit = Color(0xFF181C20),
)

private val DarkExtended = BankExtendedColors(
    heroBrush = Brush.verticalGradient(listOf(BrandColors.NavyDeep, BrandColors.Navy)),
    onHero = Color.White,
    onHeroVariant = Color.White.copy(alpha = 0.7f),
    heroAccent = BrandColors.CyanLight,
    credit = Color(0xFF6EDB9C),
    debit = Color(0xFFE1E8EF),
)

private val LocalBankColors = staticCompositionLocalOf { LightExtended }
private val LocalEventTheme = staticCompositionLocalOf { EventTheme.Default }

/** Large, rounded corners suit the Expressive style. */
private val BankShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

object BankTheme {
    val colors: BankExtendedColors
        @Composable @ReadOnlyComposable get() = LocalBankColors.current

    /** The festival being drawn right now. Never [EventTheme.Auto]. */
    val eventTheme: EventTheme
        @Composable @ReadOnlyComposable get() = LocalEventTheme.current
}

@Composable
fun ThemeMode.isDark(): Boolean = when (this) {
    ThemeMode.System -> isSystemInDarkTheme()
    ThemeMode.Light -> false
    ThemeMode.Dark -> true
}

/**
 * App theme. Dynamic color is left off on purpose: a bank app keeps its brand colors.
 * MaterialExpressiveTheme switches the motion scheme to Expressive springs.
 *
 * [eventTheme] layers a festival skin on top of light/dark. Pass the resolved theme:
 * [EventTheme.Auto] is treated as Default here.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    eventTheme: EventTheme = EventTheme.Default,
    content: @Composable () -> Unit,
) {
    val event = if (eventTheme == EventTheme.Auto) EventTheme.Default else eventTheme
    val extended = extendedColorsFor(event, darkTheme, if (darkTheme) DarkExtended else LightExtended)
    CompositionLocalProvider(LocalBankColors provides extended, LocalEventTheme provides event) {
        MaterialExpressiveTheme(
            colorScheme = colorSchemeFor(event, darkTheme),
            motionScheme = MotionScheme.expressive(),
            shapes = BankShapes,
            content = content,
        )
    }
}
