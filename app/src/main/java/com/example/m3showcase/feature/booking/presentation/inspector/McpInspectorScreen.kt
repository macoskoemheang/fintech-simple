package com.example.m3showcase.feature.booking.presentation.inspector

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.m3showcase.R
import com.example.m3showcase.core.designsystem.component.BankScreenScaffold
import com.example.m3showcase.core.designsystem.component.InfoCard
import com.example.m3showcase.core.designsystem.component.InfoRow
import com.example.m3showcase.feature.booking.domain.model.McpConnectionState
import com.example.m3showcase.feature.booking.domain.model.McpTool
import com.example.m3showcase.feature.booking.domain.model.McpTrafficEntry
import com.example.m3showcase.feature.booking.presentation.hub.McpConnectionCard
import com.example.m3showcase.core.ui.asString
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun McpInspectorRoute(onBack: () -> Unit, viewModel: McpInspectorViewModel = koinViewModel()) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                McpInspectorEvent.NavigateBack -> onBack()
            }
        }
    }
    McpInspectorScreen(state, viewModel::onAction)
}

private val Mono = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, lineHeight = 17.sp)

@Composable
fun McpInspectorScreen(state: McpInspectorViewState, onAction: (McpInspectorViewAction) -> Unit) {
    BankScreenScaffold(
        title = stringResource(R.string.booking_inspector_title),
        onBack = { onAction(McpInspectorViewAction.OnBackClicked) },
        actions = {
            IconButton(onClick = { onAction(McpInspectorViewAction.OnReconnect) }) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.booking_mcp_reconnect))
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "connection") {
                McpConnectionCard(state.connection, onClick = {}, onReconnect = { onAction(McpInspectorViewAction.OnReconnect) })
            }
            (state.connection as? McpConnectionState.Connected)?.let { connected ->
                item(key = "server") { ServerCard(connected) }
            }
            item(key = "tabs") { InspectorTabs(state.tab, state.traffic.size) { onAction(McpInspectorViewAction.OnTabSelected(it)) } }
            when (state.tab) {
                InspectorTab.Tools -> toolsTab(state, onAction)
                InspectorTab.TryIt -> item(key = "playground") { Playground(state, onAction) }
                InspectorTab.Traffic -> trafficTab(state, onAction)
            }
        }
    }
}

@Composable
private fun ServerCard(connected: McpConnectionState.Connected) {
    val server = connected.server
    InfoCard(title = stringResource(R.string.booking_inspector_server)) {
        InfoRow(stringResource(R.string.booking_inspector_name), server.name)
        InfoRow(stringResource(R.string.booking_inspector_version), server.version)
        InfoRow(stringResource(R.string.booking_inspector_protocol), server.protocolVersion)
        InfoRow(stringResource(R.string.booking_inspector_transport), server.transport)
        InfoRow(stringResource(R.string.booking_inspector_config), server.configPath)
        InfoRow(stringResource(R.string.booking_inspector_definition), server.definitionPath, divider = server.instructions != null)
        server.instructions?.let {
            Column(Modifier.padding(vertical = 12.dp)) {
                Text(stringResource(R.string.booking_inspector_instructions), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun InspectorTabs(selected: InspectorTab, trafficCount: Int, onSelect: (InspectorTab) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
        InspectorTab.entries.forEachIndexed { index, tab ->
            ToggleButton(
                checked = tab == selected,
                onCheckedChange = { onSelect(tab) },
                modifier = Modifier.weight(1f).semantics { role = Role.RadioButton },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    InspectorTab.entries.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
            ) {
                Text(
                    when (tab) {
                        InspectorTab.Tools -> stringResource(R.string.booking_inspector_tab_tools)
                        InspectorTab.TryIt -> stringResource(R.string.booking_inspector_tab_try)
                        InspectorTab.Traffic -> if (trafficCount > 0) "${stringResource(R.string.booking_inspector_tab_traffic)} · $trafficCount" else stringResource(R.string.booking_inspector_tab_traffic)
                    },
                    maxLines = 1,
                )
            }
        }
    }
}

// region Tools

private fun LazyListScope.toolsTab(state: McpInspectorViewState, onAction: (McpInspectorViewAction) -> Unit) {
    val tools = (state.connection as? McpConnectionState.Connected)?.tools.orEmpty()
    items(tools, key = { "tool-${it.name}" }) { tool -> ToolCard(tool) { onAction(McpInspectorViewAction.OnTryTool(tool.name)) } }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ToolCard(tool: McpTool, onTry: () -> Unit) {
    Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.94f), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Build, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.tertiary)
                Column(Modifier.weight(1f).padding(start = 10.dp)) {
                    Text(tool.name, style = Mono.copy(fontSize = 14.sp), fontWeight = FontWeight.Bold)
                    tool.title?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                TextButton(onClick = onTry) {
                    Icon(Icons.Rounded.PlayArrow, null, Modifier.size(18.dp))
                    Text(stringResource(R.string.booking_inspector_try), Modifier.padding(start = 4.dp))
                }
            }
            Text(tool.description, style = MaterialTheme.typography.bodySmall)
            if (tool.params.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    tool.params.forEach { param ->
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    buildString {
                                        append(param.name)
                                        if (param.required) append('*')
                                        append(": ")
                                        append(if (param.options.isNotEmpty()) param.options.joinToString("|") else param.type)
                                    },
                                    style = Mono,
                                )
                            },
                        )
                    }
                }
                Text("* ${stringResource(R.string.booking_inspector_required)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// endregion

// region Playground

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Playground(state: McpInspectorViewState, onAction: (McpInspectorViewAction) -> Unit) {
    val tools = (state.connection as? McpConnectionState.Connected)?.tools.orEmpty()
    val selected = state.selectedTool
    val playground = state.playground
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tools, key = { it.name }) { tool ->
                FilterChip(
                    selected = tool.name == selected?.name,
                    onClick = { onAction(McpInspectorViewAction.OnToolSelected(tool.name)) },
                    label = { Text(tool.name, style = Mono) },
                )
            }
        }
        OutlinedTextField(
            value = state.argumentsText,
            onValueChange = { onAction(McpInspectorViewAction.OnArgumentsChanged(it)) },
            label = { Text(stringResource(R.string.booking_inspector_arguments)) },
            textStyle = Mono.copy(fontSize = 13.sp),
            minLines = 4,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, autoCorrectEnabled = false),
            isError = playground.error != null,
            supportingText = playground.error?.let { { Text(it.asString()) } },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { onAction(McpInspectorViewAction.OnResetArguments) }, enabled = playground.arguments != null) {
                Icon(Icons.Rounded.RestartAlt, null, Modifier.size(18.dp))
                Text(stringResource(R.string.booking_inspector_reset), Modifier.padding(start = 6.dp))
            }
            Button(
                onClick = { onAction(McpInspectorViewAction.OnRunClicked) },
                enabled = selected != null && !playground.isRunning,
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
            ) {
                if (playground.isRunning) {
                    LoadingIndicator(Modifier.size(24.dp))
                } else {
                    Icon(Icons.Rounded.PlayArrow, null)
                    Text(stringResource(R.string.booking_inspector_run), Modifier.padding(start = 6.dp))
                }
            }
        }
        playground.result?.let { result ->
            val scheme = MaterialTheme.colorScheme
            Surface(
                shape = MaterialTheme.shapes.large,
                color = if (result.isError) scheme.errorContainer else scheme.surfaceContainerHigh,
                contentColor = if (result.isError) scheme.onErrorContainer else scheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row {
                        Text(
                            stringResource(if (result.isError) R.string.booking_inspector_result_error else R.string.booking_inspector_result),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                        )
                        Text("${result.toolName} · ${stringResource(R.string.booking_inspector_duration, result.durationMs.toInt())}", style = MaterialTheme.typography.labelMedium)
                    }
                    Text(result.body, style = Mono, modifier = Modifier.horizontalScroll(rememberScrollState()))
                }
            }
        }
    }
}

// endregion

// region Traffic

private fun LazyListScope.trafficTab(state: McpInspectorViewState, onAction: (McpInspectorViewAction) -> Unit) {
    item(key = "traffic-header") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.booking_inspector_traffic_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = { onAction(McpInspectorViewAction.OnClearTraffic) }, enabled = state.traffic.isNotEmpty()) {
                Icon(Icons.Rounded.DeleteSweep, null, Modifier.size(18.dp))
                Text(stringResource(R.string.booking_inspector_clear), Modifier.padding(start = 6.dp))
            }
        }
    }
    if (state.traffic.isEmpty()) {
        item(key = "traffic-empty") {
            Text(
                stringResource(R.string.booking_inspector_traffic_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 24.dp).fillMaxWidth(),
            )
        }
    }
    items(state.traffic, key = { "msg-${it.id}" }) { entry ->
        TrafficRow(entry, expanded = entry.id in state.expandedTraffic) { onAction(McpInspectorViewAction.OnTrafficToggled(entry.id)) }
    }
}

@Composable
private fun TrafficRow(entry: McpTrafficEntry, expanded: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date(entry.timestamp))
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = scheme.surfaceContainerLowest.copy(alpha = 0.94f),
        modifier = Modifier.fillMaxWidth().animateContentSize(),
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (entry.outgoing) Icons.AutoMirrored.Rounded.ArrowForward else Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(if (entry.outgoing) R.string.booking_inspector_sent else R.string.booking_inspector_received),
                    tint = when {
                        entry.isError -> scheme.error
                        entry.outgoing -> scheme.primary
                        else -> scheme.tertiary
                    },
                    modifier = Modifier.size(18.dp),
                )
                Text(entry.method, style = Mono.copy(fontSize = 13.sp), fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f).padding(start = 8.dp))
                Text(time, style = Mono, color = scheme.onSurfaceVariant)
            }
            if (expanded) {
                Text(entry.body, style = Mono, modifier = Modifier.padding(top = 8.dp).horizontalScroll(rememberScrollState()))
            }
        }
    }
}

// endregion
