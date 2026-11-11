package com.example.m3showcase.core.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.m3showcase.core.domain.model.EventTheme

/** The accent roles a festival repaints. Surfaces stay neutral so text keeps its contrast. */
internal data class FestivalAccents(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
)

internal data class FestivalPalette(
    val heroLight: List<Color>,
    val heroDark: List<Color>,
    val heroAccent: Color,
    val light: FestivalAccents,
    val dark: FestivalAccents,
)

/** Colors chosen from each festival's imagery. No logos or third-party artwork. */
internal val FestivalPalettes: Map<EventTheme, FestivalPalette> = mapOf(
    // Night river under the full moon, gold fireworks over the Tonle Sap.
    EventTheme.WaterFestival to FestivalPalette(
        heroLight = listOf(Color(0xFF0B1B4D), Color(0xFF0C5C7A)),
        heroDark = listOf(Color(0xFF060F2E), Color(0xFF0B2F52)),
        heroAccent = Color(0xFFFFCF5C),
        light = FestivalAccents(
            Color(0xFF00687A), Color.White, Color(0xFFAEECFF), Color(0xFF001F26),
            Color(0xFF7A5900), Color.White, Color(0xFFFFDEA0), Color(0xFF261A00),
        ),
        dark = FestivalAccents(
            Color(0xFF56D6F2), Color(0xFF003640), Color(0xFF004E5C), Color(0xFFAEECFF),
            Color(0xFFF5BF48), Color(0xFF402D00), Color(0xFF5C4200), Color(0xFFFFDEA0),
        ),
    ),
    // Festive red and marigold for the new year in April.
    EventTheme.KhmerNewYear to FestivalPalette(
        heroLight = listOf(Color(0xFF9E1B1B), Color(0xFFE2610C)),
        heroDark = listOf(Color(0xFF4A0A0A), Color(0xFF8A1C12)),
        heroAccent = Color(0xFFFFD54F),
        light = FestivalAccents(
            Color(0xFFB3261E), Color.White, Color(0xFFFFDAD5), Color(0xFF410001),
            Color(0xFF8A5100), Color.White, Color(0xFFFFDCBE), Color(0xFF2C1600),
        ),
        dark = FestivalAccents(
            Color(0xFFFFB4A9), Color(0xFF690003), Color(0xFF930A0A), Color(0xFFFFDAD5),
            Color(0xFFFFB870), Color(0xFF4A2800), Color(0xFF693C00), Color(0xFFFFDCBE),
        ),
    ),
    // Saffron robes and candlelight at the pagoda. Calm, not party colors.
    EventTheme.PchumBen to FestivalPalette(
        heroLight = listOf(Color(0xFF3D1A08), Color(0xFFA34E00)),
        heroDark = listOf(Color(0xFF1F0C03), Color(0xFF5C2A05)),
        heroAccent = Color(0xFFFFB74D),
        light = FestivalAccents(
            Color(0xFF8B5000), Color.White, Color(0xFFFFDCBE), Color(0xFF2C1600),
            Color(0xFF765A2F), Color.White, Color(0xFFFFDDB0), Color(0xFF2A1800),
        ),
        dark = FestivalAccents(
            Color(0xFFFFB870), Color(0xFF4A2800), Color(0xFF693C00), Color(0xFFFFDCBE),
            Color(0xFFE6C18C), Color(0xFF422C05), Color(0xFF5C421A), Color(0xFFFFDDB0),
        ),
    ),
    // The blue and red of the national flag.
    EventTheme.IndependenceDay to FestivalPalette(
        heroLight = listOf(Color(0xFF032EA1), Color(0xFF1F4FD1)),
        heroDark = listOf(Color(0xFF01134A), Color(0xFF032A8F)),
        heroAccent = Color(0xFFFF8A95),
        light = FestivalAccents(
            Color(0xFF1F45B5), Color.White, Color(0xFFDCE1FF), Color(0xFF00164F),
            Color(0xFFB3132D), Color.White, Color(0xFFFFDAD9), Color(0xFF400008),
        ),
        dark = FestivalAccents(
            Color(0xFFB6C4FF), Color(0xFF00287D), Color(0xFF0E3AA5), Color(0xFFDCE1FF),
            Color(0xFFFFB3B4), Color(0xFF680014), Color(0xFF920021), Color(0xFFFFDAD9),
        ),
    ),
)

/** The default dark scheme is navy-tinted, which fights red or saffron. Festivals use neutral greys. */
private fun ColorScheme.withNeutralDarkSurfaces() = copy(
    background = Color(0xFF121316),
    onBackground = Color(0xFFE3E2E6),
    surface = Color(0xFF121316),
    onSurface = Color(0xFFE3E2E6),
    surfaceVariant = Color(0xFF44474F),
    onSurfaceVariant = Color(0xFFC4C6D0),
    surfaceContainerLowest = Color(0xFF0D0E11),
    surfaceContainerLow = Color(0xFF1A1B1F),
    surfaceContainer = Color(0xFF1E1F23),
    surfaceContainerHigh = Color(0xFF292A2D),
    surfaceContainerHighest = Color(0xFF333438),
    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44474F),
)

private fun ColorScheme.withAccents(a: FestivalAccents) = copy(
    primary = a.primary,
    onPrimary = a.onPrimary,
    primaryContainer = a.primaryContainer,
    onPrimaryContainer = a.onPrimaryContainer,
    secondary = a.secondary,
    onSecondary = a.onSecondary,
    secondaryContainer = a.secondaryContainer,
    onSecondaryContainer = a.onSecondaryContainer,
)

internal fun colorSchemeFor(event: EventTheme, darkTheme: Boolean): ColorScheme {
    val base = if (darkTheme) DarkBankColors else LightBankColors
    val palette = FestivalPalettes[event] ?: return base
    return if (darkTheme) base.withNeutralDarkSurfaces().withAccents(palette.dark) else base.withAccents(palette.light)
}

internal fun extendedColorsFor(event: EventTheme, darkTheme: Boolean, base: BankExtendedColors): BankExtendedColors {
    val palette = FestivalPalettes[event] ?: return base
    return base.copy(
        heroBrush = Brush.verticalGradient(if (darkTheme) palette.heroDark else palette.heroLight),
        heroAccent = palette.heroAccent,
    )
}
