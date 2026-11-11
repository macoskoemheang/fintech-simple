package com.example.m3showcase.feature.showcase.presentation.sections

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import com.example.m3showcase.core.designsystem.MorphShape
import com.example.m3showcase.feature.showcase.presentation.ShowcaseIntent
import com.example.m3showcase.feature.showcase.presentation.ShowcaseState

/** The 7 M3 Expressive Tactics / Principles. */
@Composable
fun TacticsSection(
    state: ShowcaseState,
    onIntent: (ShowcaseIntent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        // Hero Card
        HeroSection(
            morphed = state.heroMorphed,
            onClick = { onIntent(ShowcaseIntent.OnHeroClicked) }
        )

        // Palette Selector for Vibrant Color Schemes
        VibrantPaletteSelector(
            activePalette = state.activePalette,
            onSelectPalette = { onIntent(ShowcaseIntent.OnPaletteChanged(it)) }
        )

        SectionHeader("THE 7 EXPRESSIVE TACTICS", "Practical design rules in action")

        TacticCard("1. Use a variety of shapes") {
            Tactic1ShapesDemo()
        }

        TacticCard("2. Apply rich and nuanced colors") {
            Tactic2ColorsDemo()
        }

        TacticCard("3. Guide attention with typography") {
            Tactic3TypographyDemo()
        }

        TacticCard("4. Contain content for emphasis") {
            Tactic4ContainmentDemo()
        }

        TacticCard("5. Add fluid and natural motion") {
            Tactic5MotionDemo()
        }

        TacticCard("6. Leverage component flexibility") {
            Tactic6FlexibilityDemo(onDemoClick = { onIntent(ShowcaseIntent.OnDemoClicked(it)) })
        }

        TacticCard("7. Combine tactics to create hero moments") {
            Tactic7HeroMomentDemo(
                morphed = state.heroMorphed,
                onClick = { onIntent(ShowcaseIntent.OnHeroClicked) }
            )
        }

        // Research Stats & Guidelines
        state.content?.let { ResearchSection(it) }
        GuidelinesSection()
    }
}

@Composable
private fun TacticCard(title: String, content: @Composable () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            content()
        }
    }
}

@Composable
private fun VibrantPaletteSelector(activePalette: String, onSelectPalette: (String) -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Vibrant Color Schemes", style = MaterialTheme.typography.titleMedium)
            Text(
                "Material 3 Expressive uses rich, expressive tone roles. Select a scheme palette below:",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Vibrant", "Expressive", "Contrast").forEach { palette ->
                    Button(
                        onClick = { onSelectPalette(palette) },
                        colors = if (activePalette == palette) {
                            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        } else {
                            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh, contentColor = MaterialTheme.colorScheme.onSurface)
                        },
                        shapes = ButtonDefaults.shapes()
                    ) { Text(palette) }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Tactic1ShapesDemo() {
    val shapes = listOf(
        MaterialShapes.Cookie9Sided, MaterialShapes.Sunny, MaterialShapes.Flower,
        MaterialShapes.Clover4Leaf, MaterialShapes.Burst, MaterialShapes.Pill,
        MaterialShapes.Pentagon, MaterialShapes.Heart, MaterialShapes.Gem, MaterialShapes.Puffy
    )
    val colors = listOf(
        MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Shapes build brand identity and playful expression:", style = MaterialTheme.typography.bodyMedium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            shapes.forEachIndexed { i, polygon ->
                Box(
                    Modifier
                        .size(64.dp)
                        .clip(polygon.toShape())
                        .background(colors[i % colors.size])
                )
            }
        }
    }
}

@Composable
private fun Tactic2ColorsDemo() {
    val cs = MaterialTheme.colorScheme
    val swatches = listOf(
        Triple("Primary Container", cs.primaryContainer, cs.onPrimaryContainer),
        Triple("Secondary Container", cs.secondaryContainer, cs.onSecondaryContainer),
        Triple("Tertiary Container", cs.tertiaryContainer, cs.onTertiaryContainer)
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Expressive tone roles create hierarchy and depth:", style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            swatches.forEach { (name, bg, fg) ->
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.large,
                    color = bg,
                    contentColor = fg
                ) {
                    Text(name, Modifier.padding(12.dp).heightIn(min = 60.dp), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Tactic3TypographyDemo() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Emphasized typography guides user focus:", style = MaterialTheme.typography.bodyMedium)
        Text("Display Medium Emphasized", style = MaterialTheme.typography.displayMediumEmphasized, color = MaterialTheme.colorScheme.primary)
        Text("Headline Large Emphasized", style = MaterialTheme.typography.headlineLargeEmphasized)
        Text("Title Medium Emphasized", style = MaterialTheme.typography.titleMediumEmphasized, color = MaterialTheme.colorScheme.tertiary)
    }
}

@Composable
private fun Tactic4ContainmentDemo() {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Outer Group (Low)", style = MaterialTheme.typography.labelLarge)
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Inner Group (High)", style = MaterialTheme.typography.labelMedium)
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Text("Highlighted Hero Content", Modifier.padding(12.dp), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Tactic5MotionDemo() {
    var toggled by rememberSaveable { mutableStateOf(false) }
    val travel = 200.dp
    val expressiveX by animateDpAsState(
        targetValue = if (toggled) travel else 0.dp,
        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
        label = "expressiveSpring"
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Spring physics add organic overshoot & bounce:", style = MaterialTheme.typography.bodyMedium)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest
        ) {
            Box(Modifier.padding(6.dp)) {
                Box(
                    Modifier
                        .offset(x = expressiveX)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
        Button(onClick = { toggled = !toggled }, shapes = ButtonDefaults.shapes()) {
            Text("Animate Bouncy Spring")
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Tactic6FlexibilityDemo(onDemoClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Component sizing (Extra Small to Extra Large):", style = MaterialTheme.typography.bodyMedium)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("XS", "Small", "Medium", "Large", "XL").forEach { label ->
                Button(
                    onClick = { onDemoClick("$label Button Flex") },
                    shapes = ButtonDefaults.shapes()
                ) { Text(label) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Tactic7HeroMomentDemo(morphed: Boolean, onClick: () -> Unit) {
    val progress by animateFloatAsState(
        targetValue = if (morphed) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
        label = "heroMorphTactic"
    )
    val morph = remember { Morph(MaterialShapes.Cookie9Sided, MaterialShapes.Sunny) }

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(MorphShape(morph, progress))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClickLabel = "Morph Hero", onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "HERO",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Text("Hero Moment: Shapes + Color + Motion + Containment combined", style = MaterialTheme.typography.titleMedium)
        }
    }
}
