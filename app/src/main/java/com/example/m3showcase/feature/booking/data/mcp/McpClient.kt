package com.example.m3showcase.feature.booking.data.mcp

import com.example.m3showcase.core.data.remote.FakeNetwork
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import java.util.concurrent.atomic.AtomicLong

/** Moves one JSON-RPC message to the server and returns its reply (null for notifications). */
fun interface McpTransport {
    suspend fun send(message: String): String?
}

/**
 * Runs the server in the same process, behind [FakeNetwork] so calls have latency and fail
 * offline like a remote server would. Swap this for a stdio or Streamable HTTP transport to
 * talk to a real MCP server; nothing above it changes.
 */
class InProcessMcpTransport(private val server: BookingMcpServer, private val network: FakeNetwork) : McpTransport {
    override suspend fun send(message: String): String? = network.call(latencyMs = 450) { server.handle(message) }
}

data class McpHandshake(
    val protocolVersion: String,
    val serverName: String,
    val serverTitle: String?,
    val serverVersion: String,
    val instructions: String?,
)

data class McpToolDescriptor(val name: String, val title: String?, val description: String, val inputSchema: JsonObject, val example: JsonObject?)

data class McpToolCallResult(val isError: Boolean, val structured: JsonElement?, val text: String)

/** One message in the client log, for the inspector. */
data class McpLogEntry(val id: Long, val outgoing: Boolean, val method: String, val body: String, val timestamp: Long, val isError: Boolean)

/** A small MCP client: handshake, tools/list, tools/call, and a log of every message. */
class McpClient(
    private val transport: McpTransport,
    private val clientName: String = "m3showcase-android",
    private val clientVersion: String = "1.0.0",
    private val now: () -> Long = System::currentTimeMillis,
) {
    private val ids = AtomicLong(1)
    private val logIds = AtomicLong(1)

    private val _log = MutableStateFlow<List<McpLogEntry>>(emptyList())
    val log: StateFlow<List<McpLogEntry>> = _log.asStateFlow()

    fun clearLog() {
        _log.value = emptyList()
    }

    suspend fun initialize(): McpHandshake {
        val result = request("initialize", buildJsonObject {
            put("protocolVersion", McpProtocolVersion)
            putJsonObject("capabilities") {}
            putJsonObject("clientInfo") {
                put("name", clientName)
                put("version", clientVersion)
            }
        }).jsonObject
        notify("notifications/initialized")
        val info = result.getValue("serverInfo").jsonObject
        return McpHandshake(
            protocolVersion = result.getValue("protocolVersion").jsonPrimitive.content,
            serverName = info.getValue("name").jsonPrimitive.content,
            serverTitle = info["title"]?.jsonPrimitive?.contentOrNull,
            serverVersion = info.getValue("version").jsonPrimitive.content,
            instructions = result["instructions"]?.jsonPrimitive?.contentOrNull,
        )
    }

    suspend fun listTools(): List<McpToolDescriptor> =
        request("tools/list", null).jsonObject.getValue("tools").jsonArray.map { element ->
            val tool = element.jsonObject
            McpToolDescriptor(
                name = tool.getValue("name").jsonPrimitive.content,
                title = tool["title"]?.jsonPrimitive?.contentOrNull,
                description = tool["description"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                inputSchema = tool["inputSchema"]?.jsonObject ?: JsonObject(emptyMap()),
                example = tool["_meta"]?.jsonObject?.get("example") as? JsonObject,
            )
        }

    suspend fun callTool(name: String, arguments: JsonObject): McpToolCallResult {
        val result = request("tools/call", buildJsonObject {
            put("name", name)
            put("arguments", arguments)
        }).jsonObject
        val text = result["content"]?.jsonArray
            ?.mapNotNull { block -> block.jsonObject.takeIf { it["type"]?.jsonPrimitive?.content == "text" }?.get("text")?.jsonPrimitive?.content }
            ?.joinToString("\n")
            .orEmpty()
        return McpToolCallResult(
            isError = (result["isError"] as? JsonPrimitive)?.booleanOrNull == true,
            structured = result["structuredContent"],
            text = text,
        )
    }

    private suspend fun request(method: String, params: JsonObject?): JsonElement {
        val id = ids.getAndIncrement()
        val raw = McpJson.encodeToString(JsonRpcRequest.serializer(), JsonRpcRequest(id = id, method = method, params = params))
        record(outgoing = true, method = method, raw = raw, isError = false)
        val reply = transport.send(raw) ?: throw McpProtocolException(JsonRpcErrorCode.InternalError, "No reply to $method")
        val response = McpJson.decodeFromString(JsonRpcResponse.serializer(), reply)
        val toolFailed = (response.result as? JsonObject)?.get("isError")?.let { (it as? JsonPrimitive)?.booleanOrNull } == true
        record(outgoing = false, method = method, raw = reply, isError = response.error != null || toolFailed)
        response.error?.let { throw McpProtocolException(it.code, it.message) }
        return response.result ?: throw McpProtocolException(JsonRpcErrorCode.InternalError, "Empty result for $method")
    }

    private suspend fun notify(method: String) {
        val raw = McpJson.encodeToString(JsonRpcRequest.serializer(), JsonRpcRequest(method = method))
        record(outgoing = true, method = method, raw = raw, isError = false)
        transport.send(raw)
    }

    private fun record(outgoing: Boolean, method: String, raw: String, isError: Boolean) {
        val pretty = runCatching { McpPrettyJson.encodeToString(JsonElement.serializer(), McpJson.parseToJsonElement(raw)) }.getOrDefault(raw)
        val entry = McpLogEntry(logIds.getAndIncrement(), outgoing, method, pretty, now(), isError)
        _log.update { (listOf(entry) + it).take(MaxLogEntries) } // Newest first.
    }

    private companion object {
        const val MaxLogEntries = 80
    }
}
