package com.example.m3showcase.feature.showcase.presentation

import com.example.m3showcase.core.designsystem.bankTopAppBarColors
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.m3showcase.feature.showcase.domain.model.ShowcaseTab
import com.example.m3showcase.feature.showcase.presentation.sections.ChatSection
import com.example.m3showcase.feature.showcase.presentation.sections.ComponentsSection
import com.example.m3showcase.feature.showcase.presentation.sections.TacticsSection
import org.koin.androidx.compose.koinViewModel

/** Stateful: owns ViewModel and one-time effects. */
@Composable
fun ShowcaseRoute(viewModel: ShowcaseViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ShowcaseEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text)
            }
        }
    }

    ShowcaseScreen(state, snackbarHostState, viewModel::onIntent)
}

/** Stateless: renders state, emits intents. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShowcaseScreen(
    state: ShowcaseState,
    snackbarHostState: SnackbarHostState,
    onIntent: (ShowcaseIntent) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var fabExpanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent, // AppBackground shows through.
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                colors = bankTopAppBarColors(),
                title = { Text("Material 3 Expressive") },
                subtitle = { Text(state.selectedTab.label) },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            NavigationBar {
                ShowcaseTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = state.selectedTab == tab,
                        onClick = { onIntent(ShowcaseIntent.OnTabSelected(tab)) },
                        icon = {
                            val icon = when (tab) {
                                ShowcaseTab.Tactics -> Icons.Filled.Star
                                ShowcaseTab.Components -> Icons.Filled.Widgets
                                ShowcaseTab.Chat -> Icons.AutoMirrored.Filled.Chat
                            }
                            Icon(icon, contentDescription = tab.label)
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.selectedTab != ShowcaseTab.Chat) {
                FloatingActionButtonMenu(
                    expanded = fabExpanded,
                    button = {
                        ToggleFloatingActionButton(
                            checked = fabExpanded,
                            onCheckedChange = { fabExpanded = it }
                        ) {
                            val icon by remember {
                                derivedStateOf { if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add }
                            }
                            Icon(icon, contentDescription = null)
                        }
                    }
                ) {
                    FloatingActionButtonMenuItem(
                        onClick = { fabExpanded = false; onIntent(ShowcaseIntent.OnHeroClicked) },
                        icon = { Icon(Icons.Filled.Star, contentDescription = null) },
                        text = { Text("Morph hero") }
                    )
                    FloatingActionButtonMenuItem(
                        onClick = { fabExpanded = false; onIntent(ShowcaseIntent.OnRetry) },
                        icon = { Icon(Icons.Filled.Refresh, contentDescription = null) },
                        text = { Text("Reload") }
                    )
                    FloatingActionButtonMenuItem(
                        onClick = { fabExpanded = false; onIntent(ShowcaseIntent.OnDemoClicked("FAB menu item clicked")) },
                        icon = { Icon(Icons.Filled.Share, contentDescription = null) },
                        text = { Text("Share") }
                    )
                }
            }
        }
    ) { padding ->
        when {
            state.isLoading -> CenteredBox(Modifier.padding(padding)) {
                LoadingIndicator(Modifier.size(72.dp))
            }
            state.error != null -> CenteredBox(Modifier.padding(padding)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(state.error, style = MaterialTheme.typography.titleMedium)
                    Button(onClick = { onIntent(ShowcaseIntent.OnRetry) }, shapes = ButtonDefaults.shapes()) {
                        Text("Retry")
                    }
                }
            }
            state.content != null -> {
                val layoutDirection = LocalLayoutDirection.current
                val contentPadding = PaddingValues(
                    start = padding.calculateStartPadding(layoutDirection) + 16.dp,
                    end = padding.calculateEndPadding(layoutDirection) + 16.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 16.dp
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                ) {
                    when (state.selectedTab) {
                        ShowcaseTab.Tactics -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                item { TacticsSection(state = state, onIntent = onIntent) }
                            }
                        }
                        ShowcaseTab.Components -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                item { ComponentsSection(onDemoClick = { onIntent(ShowcaseIntent.OnDemoClicked(it)) }) }
                            }
                        }
                        ShowcaseTab.Chat -> {
                            ChatSection(state = state, onIntent = onIntent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CenteredBox(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}
