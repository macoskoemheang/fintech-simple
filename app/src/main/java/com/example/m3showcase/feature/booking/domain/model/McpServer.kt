package com.example.m3showcase.feature.booking.domain.model

import androidx.compose.runtime.Immutable

/** What the client learned about the Booking MCP server during the handshake. */
@Immutable
data class McpServerInfo(
    val displayName: String,
    val name: String,
    val title: String?,
    val version: String,
    val protocolVersion: String,
    val transport: String,
    val configPath: String,
    val definitionPath: String,
    val instructions: String?,
)

@Immutable
data class McpToolParam(
    val name: String,
    val type: String,
    val required: Boolean,
    val description: String?,
    val options: List<String>,
)

@Immutable
data class McpTool(
    val name: String,
    val title: String?,
    val description: String,
    val params: List<McpToolParam>,
    /** Pretty-printed example arguments, ready to edit in the inspector. */
    val exampleArguments: String,
)

@Immutable
sealed interface McpConnectionState {
    data object Idle : McpConnectionState
    data object Connecting : McpConnectionState
    data class Connected(val server: McpServerInfo, val tools: List<McpTool>) : McpConnectionState
    data class Failed(val error: Throwable) : McpConnectionState
}

@Immutable
data class McpTrafficEntry(
    val id: Long,
    val outgoing: Boolean,
    val method: String,
    val body: String,
    val timestamp: Long,
    val isError: Boolean,
)

/** Result of running a tool by hand in the inspector. */
@Immutable
data class McpRawToolResult(val toolName: String, val isError: Boolean, val body: String, val durationMs: Long)
