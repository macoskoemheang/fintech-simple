package com.example.m3showcase.feature.booking

import com.example.m3showcase.core.domain.model.CalendarDate
import com.example.m3showcase.feature.booking.data.mcp.BookingMcpServer
import com.example.m3showcase.feature.booking.data.mcp.JsonRpcErrorCode
import com.example.m3showcase.feature.booking.data.mcp.McpClient
import com.example.m3showcase.feature.booking.data.mcp.McpConfig
import com.example.m3showcase.feature.booking.data.mcp.McpJson
import com.example.m3showcase.feature.booking.data.mcp.McpProtocolException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.File

/** Runs the real JSON definition from assets through the server and client, with "today" fixed. */
class BookingMcpServerTest {

    private val assets = File("src/main/assets")
    private val today = CalendarDate(2026, 11, 17)

    private fun server(): BookingMcpServer {
        val config = McpConfig.parse(File(assets, McpConfig.Path).readText())
        return BookingMcpServer(loadDefinition = { File(assets, config.server("booking").definition).readText() }, today = { today })
    }

    private fun client(server: BookingMcpServer = server()) = McpClient(transport = { server.handle(it) })

    @Test
    fun `handshake and tools come from the JSON definition`() = runTest {
        val client = client()
        val handshake = client.initialize()
        assertEquals("booking-mcp-server", handshake.serverName)
        assertEquals("2025-06-18", handshake.protocolVersion)
        val tools = client.listTools()
        assertEquals(
            listOf("search_listings", "get_listing", "check_availability", "create_booking", "list_bookings", "get_booking", "cancel_booking"),
            tools.map { it.name },
        )
        assertTrue(tools.all { it.example != null })
    }

    @Test
    fun `notifications get no reply and unknown methods are JSON-RPC errors`() {
        val server = server()
        assertNull(server.handle("""{"jsonrpc":"2.0","method":"notifications/initialized"}"""))
        val reply = McpJson.parseToJsonElement(server.handle("""{"jsonrpc":"2.0","id":9,"method":"nope"}""")!!).jsonObject
        assertEquals(JsonRpcErrorCode.MethodNotFound, reply.getValue("error").jsonObject.getValue("code").jsonPrimitive.int)
        val garbage = McpJson.parseToJsonElement(server.handle("not json")!!).jsonObject
        assertEquals(JsonRpcErrorCode.ParseError, garbage.getValue("error").jsonObject.getValue("code").jsonPrimitive.int)
    }

    @Test
    fun `search filters by category and query`() = runTest {
        val result = client().callTool("search_listings", buildJsonObject { put("category", "TOUR"); put("query", "angkor") })
        assertFalse(result.isError)
        val listings = result.structured!!.jsonObject.getValue("listings").jsonArray
        assertEquals(listOf("lst-angkor-sunrise"), listings.map { it.jsonObject.getValue("id").jsonPrimitive.content })
    }

    @Test
    fun `quote prices nights plus tax and spots blocked dates`() = runTest {
        val client = client()
        val ok = client.callTool("check_availability", buildJsonObject {
            put("listingId", "lst-angkor-lodge"); put("checkIn", "2026-12-01"); put("nights", 2); put("guests", 2)
        }).structured!!.jsonObject.getValue("quote").jsonObject
        assertTrue(ok.getValue("available").jsonPrimitive.boolean)
        assertEquals(29_000L, ok.getValue("subtotalMinor").jsonPrimitive.long)
        assertEquals(31_900L, ok.getValue("totalMinor").jsonPrimitive.long)

        val blocked = client.callTool("check_availability", buildJsonObject {
            put("listingId", "lst-angkor-lodge"); put("checkIn", "2026-12-23"); put("nights", 3); put("guests", 2)
        }).structured!!.jsonObject.getValue("quote").jsonObject
        assertFalse(blocked.getValue("available").jsonPrimitive.boolean)
        assertEquals("2026-12-24", blocked.getValue("unavailableDate").jsonPrimitive.content)
    }

    @Test
    fun `tour is priced per person`() = runTest {
        val quote = client().callTool("check_availability", buildJsonObject {
            put("listingId", "lst-angkor-sunrise"); put("checkIn", "2026-12-01"); put("nights", 5); put("guests", 3)
        }).structured!!.jsonObject.getValue("quote").jsonObject
        assertEquals(1, quote.getValue("nights").jsonPrimitive.int)
        assertEquals(10_500L, quote.getValue("subtotalMinor").jsonPrimitive.long)
    }

    @Test
    fun `create then cancel a booking, and the dates become taken`() = runTest {
        val client = client()
        val args = buildJsonObject {
            put("listingId", "lst-royal-lotus"); put("checkIn", "2026-12-10"); put("nights", 2); put("guests", 2); put("guestName", "Sok Dara")
        }
        val created = client.callTool("create_booking", args)
        assertFalse(created.isError)
        val booking = created.structured!!.jsonObject.getValue("booking").jsonObject
        val id = booking.getValue("id").jsonPrimitive.content
        assertEquals("CONFIRMED", booking.getValue("status").jsonPrimitive.content)

        val again = client.callTool("create_booking", args)
        assertTrue(again.isError)
        assertEquals("NOT_AVAILABLE", again.structured!!.jsonObject.getValue("error").jsonObject.getValue("code").jsonPrimitive.content)

        val cancelled = client.callTool("cancel_booking", buildJsonObject { put("bookingId", id) })
            .structured!!.jsonObject.getValue("booking").jsonObject
        assertEquals("CANCELLED", cancelled.getValue("status").jsonPrimitive.content)
        assertEquals(booking.getValue("totalMinor"), cancelled.getValue("refundMinor"))
    }

    @Test
    fun `past dates, too many guests and finished bookings are tool errors`() = runTest {
        val client = client()
        fun code(result: com.example.m3showcase.feature.booking.data.mcp.McpToolCallResult) =
            result.structured!!.jsonObject.getValue("error").jsonObject.getValue("code").jsonPrimitive.content

        assertEquals("PAST_DATE", code(client.callTool("check_availability", buildJsonObject {
            put("listingId", "lst-kep-bungalows"); put("checkIn", "2026-11-01"); put("nights", 1); put("guests", 1)
        })))
        assertEquals("TOO_MANY_GUESTS", code(client.callTool("check_availability", buildJsonObject {
            put("listingId", "lst-angkor-lodge"); put("checkIn", "2026-12-01"); put("nights", 1); put("guests", 5)
        })))
        assertEquals("NOT_CANCELLABLE", code(client.callTool("cancel_booking", buildJsonObject { put("bookingId", "bk-0987") })))
        assertEquals("INVALID_ARGUMENT", code(client.callTool("get_listing", JsonObject(emptyMap()))))
    }

    @Test
    fun `unknown tool is a protocol error`() = runTest {
        try {
            client().callTool("delete_everything", JsonObject(emptyMap()))
            fail("expected an error")
        } catch (e: McpProtocolException) {
            assertEquals(JsonRpcErrorCode.InvalidParams, e.code)
        }
    }

    @Test
    fun `client logs every request and response`() = runTest {
        val client = client()
        client.initialize()
        // initialize request + response, initialized notification.
        assertEquals(listOf("notifications/initialized", "initialize", "initialize"), client.log.value.map { it.method })
    }
}
