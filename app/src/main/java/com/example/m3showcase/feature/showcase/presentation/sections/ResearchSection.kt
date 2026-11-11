package com.example.m3showcase.feature.showcase.presentation.sections

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.m3showcase.feature.showcase.domain.model.ResearchStat
import com.example.m3showcase.feature.showcase.domain.model.ShowcaseContent

/** "The power of expressive design" — research numbers plus animated wavy bars. */
@Composable
fun ResearchSection(content: ShowcaseContent) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeader("THE POWER OF EXPRESSIVE DESIGN", "Not just prettier. Easier to use.")

        content.stats.chunked(2).forEachIndexed { rowIndex, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEachIndexed { i, stat ->
                    // Alternate container colors so size + color carry hierarchy.
                    val highlight = (rowIndex + i) % 2 == 0
                    StatCard(stat, highlight, Modifier.weight(1f))
                }
            }
        }

        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("How people perceive the brand", style = MaterialTheme.typography.titleMediumEmphasized)
                var go by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { go = true }
                content.brandPerception.forEach { item ->
                    val target = item.percent / 100f
                    val progress by animateFloatAsState(
                        targetValue = if (go) target else 0f,
                        animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
                        label = item.label
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(item.label, style = MaterialTheme.typography.bodyMedium)
                            Text("+${item.percent}%", style = MaterialTheme.typography.labelLarge)
                        }
                        LinearWavyProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(stat: ResearchStat, highlight: Boolean, modifier: Modifier = Modifier) {
    val colors = if (highlight) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
    Card(modifier, shape = MaterialTheme.shapes.extraLarge, colors = colors) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stat.value, style = MaterialTheme.typography.displaySmallEmphasized)
            Text(stat.label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Honest closing section: expressive is a tool, not a default for every screen. */
@Composable
fun GuidelinesSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("USE IT WISELY", "Bold, but still clear")
        GuidelineCard(
            title = "Great for",
            body = "Key actions, hero moments, onboarding, media and social surfaces, and anywhere you want people to notice the next step.",
            container = MaterialTheme.colorScheme.primaryContainer,
            content = MaterialTheme.colorScheme.onPrimaryContainer
        )
        GuidelineCard(
            title = "Be careful with",
            body = "Dense data screens, and anywhere flair would cost clarity or accessibility. Use expressive components within the system’s guidance; function comes first.",
            container = MaterialTheme.colorScheme.errorContainer,
            content = MaterialTheme.colorScheme.onErrorContainer
        )
        Text(
            "Figures shown come from Google’s published M3 Expressive research as reported in press coverage.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GuidelineCard(
    title: String,
    body: String,
    container: androidx.compose.ui.graphics.Color,
    content: androidx.compose.ui.graphics.Color
) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = container, contentColor = content),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleLargeEmphasized)
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
