package com.example.m3showcase.feature.booking.data.mcp

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/*
 * JSON-RPC 2.0, the wire format of the Model Context Protocol.
 * A request with no id is a notification: the other side never answers it.
 */

const val McpProtocolVersion = "2025-06-18"

@Serializable
data class JsonRpcRequest(
    val jsonrpc: String = "2.0",
    val id: Long? = null,
    val method: String,
    val params: JsonObject? = null,
)

@Serializable
data class JsonRpcError(val code: Int, val message: String, val data: JsonElement? = null)

@Serializable
data class JsonRpcResponse(
    val jsonrpc: String = "2.0",
    val id: Long? = null,
    val result: JsonElement? = null,
    val error: JsonRpcError? = null,
)

/** Standard JSON-RPC error codes. */
object JsonRpcErrorCode {
    const val ParseError = -32700
    const val InvalidRequest = -32600
    const val MethodNotFound = -32601
    const val InvalidParams = -32602
    const val InternalError = -32603
}

/** A JSON-RPC level failure (unknown method, bad params), as opposed to a tool that ran and failed. */
class McpProtocolException(val code: Int, message: String) : Exception(message)

val McpJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false // Leave out "error": null and "result": null.
    encodeDefaults = true // Always write "jsonrpc": "2.0".
}

/** Same as [McpJson], indented, for the inspector. */
val McpPrettyJson = Json(McpJson) { prettyPrint = true }
