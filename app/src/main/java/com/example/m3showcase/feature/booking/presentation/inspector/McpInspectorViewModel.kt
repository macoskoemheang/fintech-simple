package com.example.m3showcase.feature.booking.presentation.inspector

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m3showcase.core.ui.UiText
import com.example.m3showcase.feature.booking.domain.model.McpConnectionState
import com.example.m3showcase.feature.booking.domain.model.McpRawToolResult
import com.example.m3showcase.feature.booking.domain.model.McpTrafficEntry
import com.example.m3showcase.feature.booking.domain.usecase.ClearMcpTrafficUseCase
import com.example.m3showcase.feature.booking.domain.usecase.ConnectBookingServerUseCase
import com.example.m3showcase.feature.booking.domain.usecase.ObserveMcpConnectionUseCase
import com.example.m3showcase.feature.booking.domain.usecase.ObserveMcpTrafficUseCase
import com.example.m3showcase.feature.booking.domain.usecase.RunMcpToolUseCase
import com.example.m3showcase.feature.booking.presentation.toBookingMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class InspectorTab { Tools, TryIt, Traffic }

@Immutable
data class PlaygroundState(
    /** null until the user picks one; the first tool is used by default. */
    val toolName: String? = null,
    /** null = the selected tool's example. */
    val arguments: String? = null,
    val isRunning: Boolean = false,
    val result: McpRawToolResult? = null,
    val error: UiText? = null,
)

@Immutable
data class McpInspectorViewState(
    val connection: McpConnectionState = McpConnectionState.Idle,
    val tab: InspectorTab = InspectorTab.Tools,
    val playground: PlaygroundState = PlaygroundState(),
    val traffic: List<McpTrafficEntry> = emptyList(),
    val expandedTraffic: Set<Long> = emptySet(),
) {
    private val tools get() = (connection as? McpConnectionState.Connected)?.tools.orEmpty()
    val selectedTool get() = tools.firstOrNull { it.name == playground.toolName } ?: tools.firstOrNull()
    val argumentsText: String get() = playground.arguments ?: selectedTool?.exampleArguments.orEmpty()
}

sealed interface McpInspectorViewAction {
    data object OnBackClicked : McpInspectorViewAction
    data object OnReconnect : McpInspectorViewAction
    data class OnTabSelected(val tab: InspectorTab) : McpInspectorViewAction
    /** From the Tools tab: open that tool in the playground. */
    data class OnTryTool(val name: String) : McpInspectorViewAction
    data class OnToolSelected(val name: String) : McpInspectorViewAction
    data class OnArgumentsChanged(val text: String) : McpInspectorViewAction
    data object OnResetArguments : McpInspectorViewAction
    data object OnRunClicked : McpInspectorViewAction
    data class OnTrafficToggled(val id: Long) : McpInspectorViewAction
    data object OnClearTraffic : McpInspectorViewAction
}

sealed interface McpInspectorEvent {
    data object NavigateBack : McpInspectorEvent
}

class McpInspectorViewModel(
    observeConnection: ObserveMcpConnectionUseCase,
    observeTraffic: ObserveMcpTrafficUseCase,
    private val connect: ConnectBookingServerUseCase,
    private val runTool: RunMcpToolUseCase,
    private val clearTraffic: ClearMcpTrafficUseCase,
) : ViewModel() {

    private val tab = MutableStateFlow(InspectorTab.Tools)
    private val playground = MutableStateFlow(PlaygroundState())
    private val expanded = MutableStateFlow(emptySet<Long>())

    val viewState: StateFlow<McpInspectorViewState> =
        combine(observeConnection(), tab, playground, observeTraffic(), expanded, ::McpInspectorViewState)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), McpInspectorViewState())

    private val _events = Channel<McpInspectorEvent>(Channel.BUFFERED)
    val events: Flow<McpInspectorEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch { connect() }
    }

    fun onAction(action: McpInspectorViewAction) {
        when (action) {
            McpInspectorViewAction.OnBackClicked -> _events.trySend(McpInspectorEvent.NavigateBack)
            McpInspectorViewAction.OnReconnect -> viewModelScope.launch { connect(reconnect = true) }
            is McpInspectorViewAction.OnTabSelected -> tab.value = action.tab
            is McpInspectorViewAction.OnTryTool -> {
                playground.value = PlaygroundState(toolName = action.name)
                tab.value = InspectorTab.TryIt
            }
            is McpInspectorViewAction.OnToolSelected ->
                if (action.name != viewState.value.selectedTool?.name) playground.value = PlaygroundState(toolName = action.name)
            is McpInspectorViewAction.OnArgumentsChanged -> playground.update { it.copy(arguments = action.text, error = null) }
            McpInspectorViewAction.OnResetArguments -> playground.update { it.copy(arguments = null, error = null) }
            McpInspectorViewAction.OnRunClicked -> run()
            is McpInspectorViewAction.OnTrafficToggled -> expanded.update { if (action.id in it) it - action.id else it + action.id }
            McpInspectorViewAction.OnClearTraffic -> {
                clearTraffic()
                expanded.value = emptySet()
            }
        }
    }

    private fun run() {
        val state = viewState.value
        val tool = state.selectedTool ?: return
        if (state.playground.isRunning) return
        val arguments = state.argumentsText
        playground.update { it.copy(toolName = tool.name, isRunning = true, error = null) }
        viewModelScope.launch {
            runTool(tool.name, arguments).fold(
                onSuccess = { result -> playground.update { it.copy(isRunning = false, result = result) } },
                onFailure = { error -> playground.update { it.copy(isRunning = false, result = null, error = error.toBookingMessage()) } },
            )
        }
    }
}
