package com.example.m3showcase.feature.booking.data.mcp

import com.example.m3showcase.core.domain.model.CalendarDate
import com.example.m3showcase.core.domain.model.daysUntil
import com.example.m3showcase.core.domain.model.parseIsoDate
import com.example.m3showcase.core.domain.model.plusDays
import com.example.m3showcase.core.domain.model.toIsoString
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/**
 * The Booking MCP server. Everything it knows (handshake info, tools, mock data) comes from
 * the JSON definition in assets/mcp/booking-server.json; this class only runs the protocol and
 * the tool logic over that data.
 *
 * It speaks plain JSON-RPC strings, so the transport in between could be stdio, HTTP or, as
 * here, an in-process call. Bookings live in memory for the life of the process.
 *
 * Supported methods: initialize, notifications/initialized, ping, tools/list, tools/call.
 */
class BookingMcpServer(
    private val loadDefinition: () -> String,
    private val today: () -> CalendarDate,
) {
    private class ToolFailure(val code: String, message: String) : Exception(message)

    private val definition: JsonObject by lazy { McpJson.parseToJsonElement(loadDefinition()).jsonObject }
    private val data: BookingMockData by lazy { McpJson.decodeFromJsonElement(definition.getValue("mockData")) }
    private val listings: List<ListingDto> by lazy { data.listings }
    private val bookings: MutableList<BookingDto> by lazy { data.bookings.map { it.priced() }.toMutableList() }
    private var nextBookingNumber = 2001

    /** Handles one JSON-RPC message. Returns the response, or null for a notification. */
    fun handle(message: String): String? {
        val request = try {
            McpJson.decodeFromString<JsonRpcRequest>(message)
        } catch (e: SerializationException) {
            return encode(JsonRpcResponse(error = JsonRpcError(JsonRpcErrorCode.ParseError, "Parse error")))
        } catch (e: IllegalArgumentException) {
            return encode(JsonRpcResponse(error = JsonRpcError(JsonRpcErrorCode.ParseError, "Parse error")))
        }
        val id = request.id ?: return null // Notification, e.g. notifications/initialized.
        val response = try {
            JsonRpcResponse(id = id, result = dispatch(request.method, request.params ?: JsonObject(emptyMap())))
        } catch (e: McpProtocolException) {
            JsonRpcResponse(id = id, error = JsonRpcError(e.code, e.message.orEmpty()))
        }
        return encode(response)
    }

    private fun encode(response: JsonRpcResponse) = McpJson.encodeToString(JsonRpcResponse.serializer(), response)

    private fun dispatch(method: String, params: JsonObject): JsonElement = when (method) {
        "initialize" -> initialize()
        "ping" -> JsonObject(emptyMap())
        "tools/list" -> listTools()
        "tools/call" -> callTool(params)
        else -> throw McpProtocolException(JsonRpcErrorCode.MethodNotFound, "Method not found: $method")
    }

    private fun initialize(): JsonElement = buildJsonObject {
        put("protocolVersion", definition.getValue("protocolVersion"))
        put("capabilities", definition.getValue("capabilities"))
        put("serverInfo", definition.getValue("serverInfo"))
        definition["instructions"]?.let { put("instructions", it) }
    }

    /** The tools from the definition. Each tool's "example" moves into _meta, MCP's slot for extras. */
    private fun listTools(): JsonElement = buildJsonObject {
        put("tools", buildJsonArray {
            definition.getValue("tools").jsonArray.forEach { tool ->
                val fields = tool.jsonObject
                add(buildJsonObject {
                    fields.forEach { (key, value) -> if (key != "example") put(key, value) }
                    fields["example"]?.let { example -> putJsonObject("_meta") { put("example", example) } }
                })
            }
        })
    }

    private val toolNames: Set<String> by lazy {
        definition.getValue("tools").jsonArray.map { it.jsonObject.getValue("name").jsonPrimitive.content }.toSet()
    }

    private fun callTool(params: JsonObject): JsonElement {
        val name = params["name"]?.jsonPrimitive?.contentOrNull
            ?: throw McpProtocolException(JsonRpcErrorCode.InvalidParams, "Missing tool name")
        if (name !in toolNames) throw McpProtocolException(JsonRpcErrorCode.InvalidParams, "Unknown tool: $name")
        val args = (params["arguments"] as? JsonObject) ?: JsonObject(emptyMap())
        return try {
            val structured: JsonElement = when (name) {
                "search_listings" -> McpJson.encodeToJsonElement(searchListings(args))
                "get_listing" -> McpJson.encodeToJsonElement(ListingResult(listing(args.requireString("listingId"))))
                "check_availability" -> McpJson.encodeToJsonElement(QuoteResult(quote(args)))
                "create_booking" -> McpJson.encodeToJsonElement(BookingResult(createBooking(args)))
                "list_bookings" -> McpJson.encodeToJsonElement(listBookings(args))
                "get_booking" -> McpJson.encodeToJsonElement(BookingResult(booking(args.requireString("bookingId")).withListing()))
                "cancel_booking" -> McpJson.encodeToJsonElement(BookingResult(cancelBooking(args.requireString("bookingId"))))
                else -> throw McpProtocolException(JsonRpcErrorCode.InvalidParams, "Unknown tool: $name")
            }
            toolResult(structured, isError = false)
        } catch (e: ToolFailure) {
            // A tool that ran and failed is a normal result with isError = true, per the MCP spec.
            toolResult(McpJson.encodeToJsonElement(ToolErrorResult(ToolErrorDto(e.code, e.message.orEmpty()))), isError = true)
        }
    }

    /** MCP tool result: a text block for any client, plus structuredContent for clients that parse it. */
    private fun toolResult(structured: JsonElement, isError: Boolean): JsonElement = buildJsonObject {
        put("content", buildJsonArray {
            add(buildJsonObject {
                put("type", "text")
                put("text", McpJson.encodeToString(JsonElement.serializer(), structured))
            })
        })
        put("structuredContent", structured)
        put("isError", isError)
    }

    // region Tools

    private fun searchListings(args: JsonObject): ListingsResult {
        val query = args.optString("query")?.trim()?.lowercase().orEmpty()
        val category = args.optString("category")?.uppercase()
        val city = args.optString("city")
        val guests = args.optInt("guests")
        val matches = listings.filter { listing ->
            (query.isEmpty() || listOf(listing.name, listing.city, listing.description).any { query in it.lowercase() }) &&
                (category == null || listing.category == category) &&
                (city == null || listing.city.equals(city, ignoreCase = true)) &&
                (guests == null || guests <= listing.maxGuests)
        }
        return ListingsResult(matches, matches.size)
    }

    private fun listing(id: String): ListingDto =
        listings.firstOrNull { it.id == id } ?: throw ToolFailure("NOT_FOUND", "Listing $id does not exist")

    private fun quote(args: JsonObject): QuoteDto {
        val listing = listing(args.requireString("listingId"))
        val checkIn = args.requireDate("checkIn")
        val guests = args.requireInt("guests")
        val nights = if (listing.unit == "PERSON") 1 else args.requireInt("nights")
        if (nights !in 1..30) throw ToolFailure("INVALID_ARGUMENT", "nights must be between 1 and 30")
        if (guests < 1) throw ToolFailure("INVALID_ARGUMENT", "guests must be at least 1")
        if (guests > listing.maxGuests) throw ToolFailure("TOO_MANY_GUESTS", "${listing.name} fits at most ${listing.maxGuests} guests")
        if (checkIn < today()) throw ToolFailure("PAST_DATE", "checkIn ${checkIn.toIsoString()} is in the past")

        val stay = (0 until nights).map { checkIn.plusDays(it.toLong()) }
        val blocked = stay.firstOrNull { it.toIsoString() in listing.blockedDates }
            ?: stay.firstOrNull { date -> listing.unit == "NIGHT" && bookings.any { it.listingId == listing.id && it.status == "CONFIRMED" && it.covers(date) } }
        return priceOf(listing, checkIn, nights, guests).copy(available = blocked == null, unavailableDate = blocked?.toIsoString())
    }

    private fun createBooking(args: JsonObject): BookingDto {
        val guestName = args.requireString("guestName").trim()
        if (guestName.isEmpty()) throw ToolFailure("INVALID_ARGUMENT", "guestName must not be empty")
        val quote = quote(args)
        if (!quote.available) throw ToolFailure("NOT_AVAILABLE", "Not available on ${quote.unavailableDate}")
        val number = nextBookingNumber++
        val booking = BookingDto(
            id = "bk-$number",
            listingId = quote.listingId,
            checkIn = quote.checkIn,
            nights = quote.nights,
            guests = quote.guests,
            guestName = guestName,
            note = args.optString("note").orEmpty().trim(),
            status = "CONFIRMED",
            confirmationCode = confirmationCode(number),
            createdAt = today().toIsoString(),
        ).priced()
        bookings += booking
        return booking.withListing()
    }

    private fun listBookings(args: JsonObject): BookingsResult {
        val status = args.optString("status")?.uppercase()
        val all = bookings.map { it.withLiveStatus() }
        return BookingsResult(
            all.filter { status == null || it.status == status }
                .sortedByDescending { it.checkIn }
                .map { it.withListing() },
        )
    }

    private fun booking(id: String): BookingDto =
        bookings.firstOrNull { it.id == id }?.withLiveStatus() ?: throw ToolFailure("NOT_FOUND", "Booking $id does not exist")

    private fun cancelBooking(id: String): BookingDto {
        val booking = booking(id)
        val checkIn = parseIsoDate(booking.checkIn)
        if (booking.status != "CONFIRMED" || checkIn <= today()) {
            throw ToolFailure("NOT_CANCELLABLE", "Only upcoming confirmed bookings can be cancelled")
        }
        val listing = listing(booking.listingId)
        val freeCancel = listing.cancellation == "FREE_48H" && today().daysUntil(checkIn) >= 2
        val cancelled = booking.copy(status = "CANCELLED", refundMinor = if (freeCancel) booking.totalMinor else 0L)
        bookings[bookings.indexOfFirst { it.id == id }] = cancelled
        return cancelled.withListing()
    }

    // endregion

    private fun priceOf(listing: ListingDto, checkIn: CalendarDate, nights: Int, guests: Int): QuoteDto {
        val units = if (listing.unit == "PERSON") guests else nights
        val subtotal = listing.priceMinor * units
        val tax = subtotal * data.taxRateBasisPoints / 10_000
        return QuoteDto(
            listingId = listing.id,
            checkIn = checkIn.toIsoString(),
            checkOut = checkIn.plusDays(if (listing.unit == "PERSON") 0 else nights.toLong()).toIsoString(),
            nights = nights,
            guests = guests,
            unitPriceMinor = listing.priceMinor,
            subtotalMinor = subtotal,
            taxMinor = tax,
            totalMinor = subtotal + tax,
            currency = data.currency,
            available = true,
        )
    }

    /** Fills in check-out and the price; seed bookings in the JSON only carry the inputs. */
    private fun BookingDto.priced(): BookingDto {
        val price = priceOf(listing(listingId), parseIsoDate(checkIn), nights, guests)
        return copy(
            checkOut = price.checkOut,
            subtotalMinor = price.subtotalMinor,
            taxMinor = price.taxMinor,
            totalMinor = price.totalMinor,
            currency = price.currency,
        )
    }

    /** A confirmed stay whose check-out has passed reads as completed. */
    private fun BookingDto.withLiveStatus(): BookingDto =
        if (status == "CONFIRMED" && parseIsoDate(checkOut ?: checkIn) < today()) copy(status = "COMPLETED") else this

    private fun BookingDto.withListing() = copy(listing = listing(listingId))

    private fun BookingDto.covers(date: CalendarDate): Boolean {
        val start = parseIsoDate(checkIn)
        return date >= start && date < start.plusDays(nights.toLong())
    }

    private fun confirmationCode(number: Int): String {
        val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        var n = number * 7_919L + 104_729L
        val code = buildString { repeat(5) { append(alphabet[(n % alphabet.length).toInt()]); n /= alphabet.length } }
        return "BKH-$code"
    }

    // region Argument helpers

    private fun JsonObject.optString(key: String): String? =
        (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content?.takeIf { it.isNotBlank() }

    private fun JsonObject.optInt(key: String): Int? = (this[key] as? JsonPrimitive)?.intOrNull

    private fun JsonObject.requireString(key: String): String =
        (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content
            ?: throw ToolFailure("INVALID_ARGUMENT", "$key is required and must be a string")

    private fun JsonObject.requireInt(key: String): Int =
        optInt(key) ?: throw ToolFailure("INVALID_ARGUMENT", "$key is required and must be an integer")

    private fun JsonObject.requireDate(key: String): CalendarDate {
        val text = requireString(key)
        return runCatching { parseIsoDate(text) }.getOrNull()
            ?: throw ToolFailure("INVALID_ARGUMENT", "$key must be a date like 2026-12-20")
    }

    // endregion
}
