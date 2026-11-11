package com.example.m3showcase.feature.showcase.presentation.sections

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.m3showcase.feature.showcase.domain.model.ExpressiveElement

/** Five building blocks: color, shape, size, motion, containment — each with a live demo. */
@Composable
fun ElementsSection(
    selected: ExpressiveElement,
    onSelected: (ExpressiveElement) -> Unit,
    onDemoClick: (String) -> Unit
) {
    val fade = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

    Column(
        modifier = Modifier.animateContentSize(MaterialTheme.motionScheme.defaultSpatialSpec()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader("FIVE BUILDING BLOCKS", "Pick one and play")

        // Non-connected toggle buttons: shapes morph between pressed / selected / rest.
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExpressiveElement.entries.forEach { element ->
                ToggleButton(
                    checked = element == selected,
                    onCheckedChange = { onSelected(element) }
                ) { Text(element.title) }
            }
        }

        Text(selected.tagline, style = MaterialTheme.typography.titleMediumEmphasized)

        AnimatedContent(
            targetState = selected,
            transitionSpec = { fadeIn(fade) togetherWith fadeOut(fade) },
            label = "elementDemo"
        ) { element ->
            when (element) {
                ExpressiveElement.Color -> ColorDemo()
                ExpressiveElement.Shape -> ShapeDemo()
                ExpressiveElement.Size -> SizeDemo(onDemoClick)
                ExpressiveElement.Motion -> MotionDemo()
                ExpressiveElement.Containment -> ContainmentDemo()
            }
        }
    }
}

@Composable
private fun ColorDemo() {
    val cs = MaterialTheme.colorScheme
    val swatches = listOf(
        Triple("Primary", cs.primary, cs.onPrimary),
        Triple("Secondary", cs.secondary, cs.onSecondary),
        Triple("Tertiary", cs.tertiary, cs.onTertiary),
        Triple("Primary container", cs.primaryContainer, cs.onPrimaryContainer),
        Triple("Secondary container", cs.secondaryContainer, cs.onSecondaryContainer),
        Triple("Tertiary container", cs.tertiaryContainer, cs.onTertiaryContainer)
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        swatches.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (name, bg, fg) ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.large,
                        color = bg,
                        contentColor = fg
                    ) {
                        Text(name, Modifier.padding(12.dp).heightIn(min = 56.dp), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ShapeDemo() {
    val shapes = listOf(
        MaterialShapes.Cookie9Sided, MaterialShapes.Sunny, MaterialShapes.Flower,
        MaterialShapes.Clover4Leaf, MaterialShapes.Burst, MaterialShapes.Pill,
        MaterialShapes.Pentagon, MaterialShapes.Heart, MaterialShapes.Gem, MaterialShapes.Puffy
    )
    val colors = listOf(
        MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        shapes.forEachIndexed { i, polygon ->
            Box(
                Modifier
                    .size(72.dp)
                    .clip(polygon.toShape())
                    .background(colors[i % colors.size])
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SizeDemo(onDemoClick: (String) -> Unit) {
    val sizes: List<Pair<String, Dp>> = listOf(
        "Extra small" to ButtonDefaults.ExtraSmallContainerHeight,
        "Small" to ButtonDefaults.MinHeight,
        "Medium" to ButtonDefaults.MediumContainerHeight,
        "Large" to ButtonDefaults.LargeContainerHeight,
        "Extra large" to ButtonDefaults.ExtraLargeContainerHeight
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.Start) {
        sizes.forEach { (label, height) ->
            Button(
                onClick = { onDemoClick("$label button, ${height.value.toInt()}dp tall") },
                modifier = Modifier.heightIn(height),
                contentPadding = ButtonDefaults.contentPaddingFor(height),
                shapes = ButtonDefaults.shapes()
            ) { Text(label, style = ButtonDefaults.textStyleFor(height)) }
        }
    }
}

/** Same move, two feels: a fixed-duration tween vs. the theme's expressive spring. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MotionDemo() {
    var toggled by rememberSaveable { mutableStateOf(false) }
    val travel = 220.dp
    val standardX by animateDpAsState(if (toggled) travel else 0.dp, tween(400), label = "standard")
    val expressiveX by animateDpAsState(
        targetValue = if (toggled) travel else 0.dp,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "expressive"
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MotionTrack("Standard tween", standardX, MaterialTheme.colorScheme.outline)
        MotionTrack("Expressive spring", expressiveX, MaterialTheme.colorScheme.primary)
        Button(onClick = { toggled = !toggled }, shapes = ButtonDefaults.shapes()) { Text("Play") }
    }
}

@Composable
private fun MotionTrack(label: String, x: Dp, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest
        ) {
            Box(Modifier.padding(4.dp)) {
                Box(Modifier.offset(x = x).size(40.dp).clip(CircleShape).background(color))
            }
        }
    }
}

@Composable
private fun ContainmentDemo() {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Outer group", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(start = 8.dp))
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Inner group", style = MaterialTheme.typography.labelLarge)
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Text("The thing that matters", Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
