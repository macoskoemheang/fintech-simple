package com.example.m3showcase.feature.showcase.presentation.sections

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import com.example.m3showcase.core.designsystem.MorphShape

@Composable
internal fun SectionHeader(overline: String, title: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(overline, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(title, style = MaterialTheme.typography.headlineMediumEmphasized)
    }
}

/** "What's Material 3 Expressive?" — a tappable shape that morphs on an expressive spring. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HeroSection(morphed: Boolean, onClick: () -> Unit) {
    // Spatial spring from the theme's MotionScheme: it can overshoot, which gives the "bounce".
    val progress by animateFloatAsState(
        targetValue = if (morphed) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
        label = "heroMorph"
    )
    val morph = remember { Morph(MaterialShapes.Cookie9Sided, MaterialShapes.Sunny) }

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(MorphShape(morph, progress))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClickLabel = "Morph shape", onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "M3E",
                    style = MaterialTheme.typography.displayMediumEmphasized,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Text(
                "What’s Material 3 Expressive?",
                style = MaterialTheme.typography.headlineLargeEmphasized,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                "The most researched update to Material Design. It uses color, shape, size, " +
                    "motion and containment to make apps feel more personal, and easier to use.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                "Tap the shape",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}
