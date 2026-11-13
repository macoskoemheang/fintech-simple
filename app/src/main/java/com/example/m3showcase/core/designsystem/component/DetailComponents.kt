package com.example.m3showcase.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.m3showcase.R
import com.example.m3showcase.core.designsystem.bankTopAppBarColors
import com.example.m3showcase.core.ui.Loadable
import com.example.m3showcase.core.ui.asString

/** Transparent scaffold with a back arrow, used by every feature/detail screen. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankScreenScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent, // AppBackground shows through.
        topBar = {
            TopAppBar(
                colors = bankTopAppBarColors(),
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = actions,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = content,
    )
}

/** Loading → full-screen loader, Error → retry, Success → [content]. */
@Composable
fun <T> LoadableContent(state: Loadable<T>, onRetry: () -> Unit, modifier: Modifier = Modifier, content: @Composable (T) -> Unit) {
    Box(modifier.fillMaxSize()) {
        when (state) {
            Loadable.Loading -> LoadingState()
            is Loadable.Error -> ErrorState(state.message.asString(), onRetry, isOffline = state.isOffline)
            is Loadable.Success -> content(state.data)
        }
    }
}

/** Rounded surface grouping [InfoRow]s, with an optional section title above it. */
@Composable
fun InfoCard(modifier: Modifier = Modifier, title: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (title != null) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.94f),
            modifier = Modifier.fillMaxWidth(),
        ) { Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp), content = content) }
    }
}

/** Label on the left, value on the right; [divider] draws a hairline under it. */
@Composable
fun InfoRow(label: String, value: String, divider: Boolean = true, valueColor: Color = Color.Unspecified, strong: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.Top) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f).padding(end = 12.dp),
        )
        Text(
            value,
            style = if (strong) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (strong) FontWeight.Bold else FontWeight.Medium,
            color = valueColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.4f),
        )
    }
    if (divider) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}

/**
 * Big gradient header for a detail screen: shaped icon, label, the main amount and a subtitle.
 * Text is white, so [palette] must be dark enough (all AccountCardPalette colors are).
 */
@Composable
fun HeroCard(
    icon: ImageVector,
    iconShape: Shape,
    label: String,
    amount: String,
    subtitle: String?,
    palette: AccountCardPalette,
    modifier: Modifier = Modifier,
    badge: @Composable (() -> Unit)? = null,
    bottom: @Composable (ColumnScope.() -> Unit)? = null,
) {
    Surface(shape = RoundedCornerShape(28.dp), color = Color.Transparent, contentColor = Color.White, modifier = modifier.fillMaxWidth()) {
        Column(Modifier.accountGradient(palette).padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = iconShape, color = Color.White.copy(alpha = 0.18f), contentColor = Color.White, modifier = Modifier.size(44.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(icon, null, Modifier.size(24.dp)) }
                }
                Spacer(Modifier.weight(1f))
                badge?.invoke()
            }
            Spacer(Modifier.height(18.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.74f))
            Text(amount, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.74f)) }
            bottom?.let {
                Spacer(Modifier.height(16.dp))
                it()
            }
        }
    }
}

/** Expressive wavy progress with captions underneath ("12 of 24 paid", "50%"). */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CaptionedProgress(progress: Float, start: String, end: String, onDark: Boolean = false) {
    val track = if (onDark) Color.White.copy(alpha = 0.24f) else MaterialTheme.colorScheme.surfaceVariant
    val bar = if (onDark) Color.White else MaterialTheme.colorScheme.primary
    val caption = if (onDark) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        LinearWavyProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            color = bar,
            trackColor = track,
            modifier = Modifier.fillMaxWidth(),
        )
        Row {
            Text(start, style = MaterialTheme.typography.labelMedium, color = caption, modifier = Modifier.weight(1f))
            Text(end, style = MaterialTheme.typography.labelMedium, color = caption)
        }
    }
}

/** Small rounded status label, e.g. "Paid", "Pending", "Open now". */
@Composable
fun StatusPill(text: String, container: Color, content: Color, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(50), color = container, contentColor = content, modifier = modifier) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

/** Two-up summary tile used at the top of list screens. */
@Composable
fun SummaryTile(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.94f), modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            // Multi-currency totals put one currency per line.
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 3)
        }
    }
}

val ScreenContentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp)
