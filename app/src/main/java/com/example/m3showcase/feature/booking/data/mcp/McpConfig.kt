package com.example.m3showcase.feature.booking.data.mcp

import kotlinx.serialization.Serializable

/** assets/mcp/mcp.json: which MCP servers the app connects to. */
@Serializable
data class McpConfig(val mcpServers: Map<String, McpServerEntry>) {
    fun server(key: String): McpServerEntry =
        mcpServers[key]?.takeIf { it.enabled } ?: error("MCP server \"$key\" is missing or disabled in $Path")

    companion object {
        const val Path = "mcp/mcp.json"
        fun parse(json: String): McpConfig = McpJson.decodeFromString(serializer(), json)
    }
}

@Serializable
data class McpServerEntry(
    val displayName: String,
    val description: String = "",
    val transport: String,
    val definition: String,
    val enabled: Boolean = true,
)

/** Reads a file from the app's assets; a plain file read in JVM tests. */
fun interface AssetReader {
    fun read(path: String): String
}
