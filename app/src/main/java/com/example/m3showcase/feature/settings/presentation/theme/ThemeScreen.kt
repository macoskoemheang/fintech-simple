package com.example.m3showcase.feature.settings.presentation.theme

import com.example.m3showcase.core.designsystem.bankTopAppBarColors
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.m3showcase.core.designsystem.AppTheme
import com.example.m3showcase.core.designsystem.BankTheme
import com.example.m3showcase.core.designsystem.component.LoadingState
import com.example.m3showcase.core.domain.model.ThemeMode
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Switch
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextOverflow
import com.example.m3showcase.core.designsystem.FestivalHeroArt
import com.example.m3showcase.core.domain.model.EventTheme
import com.example.m3showcase.core.ui.displayName
import com.example.m3showcase.core.ui.khmerName
import org.koin.androidx.compose.koinViewModel

@Composable
fun ThemeRoute(
    onBack: () -> Unit,
    viewModel: ThemeViewModel = koinViewModel(),
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ThemeEvent.NavigateBack -> onBack()
            }
        }
    }
    ThemeScreen(state, viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeScreen(state: ThemeViewState, onAction: (ThemeViewAction) -> Unit) {
    Scaffold(
        containerColor = Color.Transparent, // AppBackground shows through.
        topBar = {
            TopAppBar(
                colors = bankTopAppBarColors(),
                title = { Text("Theme") },
                navigationIcon = {
                    IconButton(onClick = { onAction(ThemeViewAction.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                ThemeViewState.Loading -> LoadingState()
                is ThemeViewState.Success -> ThemeContent(state, onAction)
            }
        }
    }
}

@Composable
private fun ThemeContent(state: ThemeViewState.Success, onAction: (ThemeViewAction) -> Unit) {
    val selected = state.selected
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(
            "Choose how the app looks. Changes apply right away.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Same order as the old selector: Light, Dark, System.
        Row(
            Modifier.fillMaxWidth().selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            listOf(ThemeMode.Light, ThemeMode.Dark, ThemeMode.System).forEach { mode ->
                ThemeOptionCard(
                    mode = mode,
                    selected = mode == selected,
                    onClick = { onAction(ThemeViewAction.OnThemeSelected(mode)) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        SystemInfoCard(selected)

        FestivalSection(state, onAction)
    }
}

/** ABA-style seasonal skins. Colors and hero artwork change; light/dark stays as picked above. */
@Composable
private fun FestivalSection(state: ThemeViewState.Success, onAction: (ThemeViewAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Festival themes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
        ) {
            ListItem(
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.toggleable(
                    value = state.isAutoEvent,
                    role = Role.Switch,
                    onValueChange = { onAction(ThemeViewAction.OnAutoEventToggled(it)) },
                ),
                leadingContent = { Icon(Icons.Rounded.Celebration, null, tint = MaterialTheme.colorScheme.primary) },
                headlineContent = { Text("Match the season") },
                supportingContent = {
                    Text(
                        state.nextFestivalLabel?.let { "Switches on for each festival. Next: $it" }
                            ?: "Switches on for each festival.",
                    )
                },
                // The row handles the toggle, so the Switch itself isn't separately clickable.
                trailingContent = { Switch(checked = state.isAutoEvent, onCheckedChange = null) },
            )
        }

        // Two columns; a Column of Rows because we're already inside a vertical scroll.
        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.eventOptions.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { option ->
                        FestivalCard(
                            option = option,
                            // With Auto on, highlight what Auto picked, but mark it as automatic.
                            selected = option.theme == state.appliedEvent,
                            isAuto = state.isAutoEvent,
                            onClick = { onAction(ThemeViewAction.OnEventThemeSelected(option.theme)) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FestivalCard(
    option: EventThemeOption,
    selected: Boolean,
    isAuto: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderWidth by animateDpAsState(if (selected) 3.dp else 1.dp, label = "festivalBorder")
    val borderColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        label = "festivalBorderColor",
    )
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Surface(
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(borderWidth, borderColor),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        modifier = modifier.selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
    ) {
        Column {
            // The preview renders with that festival's real theme, so it can't drift from the app.
            AppTheme(darkTheme = darkTheme, eventTheme = option.theme) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.6f)
                        .background(BankTheme.colors.heroBrush),
                ) {
                    FestivalHeroArt(modifier = Modifier.matchParentSize(), animate = false)
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Box(Modifier.size(12.dp).background(BankTheme.colors.heroAccent, CircleShape))
                        Bar(width = 40.dp, color = Color.White.copy(alpha = 0.6f), height = 3.dp)
                        Bar(width = 56.dp, color = Color.White, height = 6.dp)
                    }
                    if (option.isLive) {
                        Surface(
                            shape = CircleShape,
                            color = BankTheme.colors.heroAccent,
                            modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                        ) {
                            Text(
                                "Live now",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F1300),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                        }
                    }
                    if (selected) {
                        Surface(
                            shape = MaterialShapes.Cookie9Sided.toShape(),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(26.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    if (isAuto) Icons.Rounded.AutoAwesome else Icons.Rounded.Check,
                                    contentDescription = if (isAuto) "Chosen automatically" else null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                        }
                    }
                }
            }
            Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(
                    option.theme.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    option.theme.khmerName ?: "Everyday look",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
                option.dateLabel?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/** A selectable card showing a miniature of the app in that theme. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ThemeOptionCard(
    mode: ThemeMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Expressive springs: the chosen card pops slightly and its border grows.
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.94f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "scale",
    )
    val borderWidth by animateDpAsState(if (selected) 3.dp else 1.dp, label = "border")
    val borderColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        label = "borderColor",
    )

    Column(
        modifier = modifier.selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.scale(scale)) {
            Surface(
                shape = MaterialTheme.shapes.large,
                border = BorderStroke(borderWidth, borderColor),
                modifier = Modifier.fillMaxWidth().aspectRatio(0.56f),
            ) {
                when (mode) {
                    ThemeMode.Light -> MiniPreview(darkTheme = false)
                    ThemeMode.Dark -> MiniPreview(darkTheme = true)
                    ThemeMode.System -> Box {
                        MiniPreview(darkTheme = false)
                        // Diagonal split: light top-left, dark bottom-right.
                        Box(Modifier.clip(DiagonalShape)) { MiniPreview(darkTheme = true) }
                    }
                }
            }
            if (selected) {
                Surface(
                    shape = MaterialShapes.Cookie9Sided.toShape(),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(28.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Check, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            mode.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

private val DiagonalShape = GenericShape { size, _ ->
    moveTo(size.width, 0f)
    lineTo(size.width, size.height)
    lineTo(0f, size.height)
    close()
}

/**
 * A miniature Home screen. It uses the real AppTheme for the given mode, so the
 * preview can never drift from the app's actual colors.
 */
@Composable
private fun MiniPreview(darkTheme: Boolean) {
    AppTheme(darkTheme = darkTheme, eventTheme = BankTheme.eventTheme) {
        val colors = MaterialTheme.colorScheme
        Column(Modifier.fillMaxSize().background(colors.background)) {
            // Navy hero with an account card
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp))
                    .background(BankTheme.colors.heroBrush)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).background(BankTheme.colors.heroAccent, CircleShape))
                    Spacer(Modifier.width(4.dp))
                    Bar(width = 28.dp, color = Color.White.copy(alpha = 0.8f))
                }
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Bar(width = 24.dp, color = Color.White.copy(alpha = 0.6f), height = 3.dp)
                    Bar(width = 40.dp, color = Color.White, height = 6.dp)
                }
            }
            // Services grid
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(6.dp)
                    .background(colors.surfaceContainerLowest, RoundedCornerShape(8.dp))
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                repeat(4) { Box(Modifier.size(10.dp).background(colors.primaryContainer, CircleShape)) }
            }
            // Transaction rows
            repeat(3) {
                Row(Modifier.padding(horizontal = 8.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(9.dp).background(colors.surfaceContainerHighest, CircleShape))
                    Spacer(Modifier.width(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Bar(width = 26.dp, color = colors.onSurface.copy(alpha = 0.7f))
                        Bar(width = 16.dp, color = colors.onSurfaceVariant.copy(alpha = 0.5f), height = 2.dp)
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            // Bottom navigation
            Row(
                Modifier.fillMaxWidth().background(colors.surfaceContainer).padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                repeat(4) { index ->
                    Box(
                        Modifier
                            .size(width = 12.dp, height = 6.dp)
                            .background(if (index == 0) colors.primary else colors.onSurfaceVariant.copy(alpha = 0.4f), CircleShape),
                    )
                }
            }
        }
    }
}

@Composable
private fun Bar(width: Dp, color: Color, height: Dp = 4.dp) {
    Box(Modifier.size(width = width, height = height).background(color, CircleShape))
}

@Composable
private fun SystemInfoCard(selected: ThemeMode) {
    val systemDark = isSystemInDarkTheme()
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Info, null, tint = MaterialTheme.colorScheme.primary)
            Text(
                text = when (selected) {
                    ThemeMode.System -> "Following your device. It's in ${if (systemDark) "dark" else "light"} mode right now."
                    ThemeMode.Light -> "Always light, whatever your device setting."
                    ThemeMode.Dark -> "Always dark. Easier on the eyes at night."
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
    }
}

@Preview
@Composable
private fun ThemeScreenPreview() {
    AppTheme {
        ThemeScreen(
            ThemeViewState.Success(
                selected = ThemeMode.System,
                isAutoEvent = true,
                appliedEvent = EventTheme.WaterFestival,
                eventOptions = listOf(EventThemeOption(EventTheme.Default, null, false)) +
                    EventTheme.festivals.map { EventThemeOption(it, "23–25 Nov 2026", it == EventTheme.WaterFestival) },
                nextFestivalLabel = "Water Festival · 23–25 Nov 2026",
            ),
        ) {}
    }
}
