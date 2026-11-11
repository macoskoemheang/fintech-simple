package com.example.m3showcase.core.designsystem

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/*
 * Brand palette inspired by ABA Mobile: deep navy hero areas with cyan accents.
 * The colors are our own; no ABA logos or trademarks are used.
 */
object BrandColors {
    val NavyDeep = Color(0xFF041A2E)
    val Navy = Color(0xFF0A2E4D)
    val NavyLight = Color(0xFF12426B)
    val Cyan = Color(0xFF00A9CC)
    val CyanLight = Color(0xFF6FD8F0)
    val Coral = Color(0xFFE5484D)
    val Mint = Color(0xFF2FBF71)
}

internal val LightBankColors = lightColorScheme(
    primary = Color(0xFF005A8C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDE5FF),
    onPrimaryContainer = Color(0xFF001D32),
    secondary = Color(0xFF006879),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB5EBFF),
    onSecondaryContainer = Color(0xFF001F26),
    tertiary = Color(0xFF9C4146),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDADB),
    onTertiaryContainer = Color(0xFF40000A),
    background = Color(0xFFF4F7FB),
    onBackground = Color(0xFF181C20),
    surface = Color(0xFFF4F7FB),
    onSurface = Color(0xFF181C20),
    surfaceVariant = Color(0xFFDDE3EA),
    onSurfaceVariant = Color(0xFF41484F),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFEEF2F7),
    surfaceContainer = Color(0xFFE8EDF3),
    surfaceContainerHigh = Color(0xFFE2E8EF),
    surfaceContainerHighest = Color(0xFFDCE3EB),
    outline = Color(0xFF71787F),
    outlineVariant = Color(0xFFC1C7CF),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

internal val DarkBankColors = darkColorScheme(
    primary = Color(0xFF6FD3F0),
    onPrimary = Color(0xFF003544),
    primaryContainer = Color(0xFF0E4D66),
    onPrimaryContainer = Color(0xFFBDEBFF),
    secondary = Color(0xFF9ACBE8),
    onSecondary = Color(0xFF00344B),
    secondaryContainer = Color(0xFF1A3D57),
    onSecondaryContainer = Color(0xFFCBE6FF),
    tertiary = Color(0xFFFFB3B3),
    onTertiary = Color(0xFF5F1419),
    tertiaryContainer = Color(0xFF7E2A30),
    onTertiaryContainer = Color(0xFFFFDADB),
    background = Color(0xFF071A2C),
    onBackground = Color(0xFFE1E8EF),
    surface = Color(0xFF071A2C),
    onSurface = Color(0xFFE1E8EF),
    surfaceVariant = Color(0xFF1F3A52),
    onSurfaceVariant = Color(0xFFAAB8C6),
    surfaceContainerLowest = Color(0xFF041322),
    surfaceContainerLow = Color(0xFF0B2236),
    surfaceContainer = Color(0xFF0F283F),
    surfaceContainerHigh = Color(0xFF143149),
    surfaceContainerHighest = Color(0xFF1A3A55),
    outline = Color(0xFF73879A),
    outlineVariant = Color(0xFF2A4560),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)
