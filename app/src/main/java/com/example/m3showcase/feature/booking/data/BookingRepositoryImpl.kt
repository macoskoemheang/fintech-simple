package com.example.m3showcase.feature.booking.data

import com.example.m3showcase.core.common.DispatcherProvider
import com.example.m3showcase.core.domain.model.BankError
import com.example.m3showcase.core.domain.model.CalendarDate
import com.example.m3showcase.core.domain.model.CurrencyCode
import com.example.m3showcase.core.domain.model.Money
import com.example.m3showcase.core.domain.model.parseIsoDate
import com.example.m3showcase.core.domain.model.toIsoString
import com.example.m3showcase.feature.booking.data.mcp.AssetReader
import com.example.m3showcase.feature.booking.data.mcp.BookingDto
import com.example.m3showcase.feature.booking.data.mcp.BookingResult
import com.example.m3showcase.feature.booking.data.mcp.BookingsResult
import com.example.m3showcase.feature.booking.data.mcp.ListingDto
import com.example.m3showcase.feature.booking.data.mcp.ListingResult
import com.example.m3showcase.feature.booking.data.mcp.ListingsResult
import com.example.m3showcase.feature.booking.data.mcp.McpClient
import com.example.m3showcase.feature.booking.data.mcp.McpConfig
import com.example.m3showcase.feature.booking.data.mcp.McpJson
import com.example.m3showcase.feature.booking.data.mcp.McpPrettyJson
import com.example.m3showcase.feature.booking.data.mcp.McpProtocolException
import com.example.m3showcase.feature.booking.data.mcp.McpToolDescriptor
import com.example.m3showcase.feature.booking.data.mcp.QuoteDto
import com.example.m3showcase.feature.booking.data.mcp.QuoteResult
import com.example.m3showcase.feature.booking.data.mcp.ToolErrorResult
import com.example.m3showcase.feature.booking.domain.model.Amenity
import com.example.m3showcase.feature.booking.domain.model.Booking
import com.example.m3showcase.feature.booking.domain.model.BookingError
import com.example.m3showcase.feature.booking.domain.model.BookingQuote
import com.example.m3showcase.feature.booking.domain.model.BookingRequest
import com.example.m3showcase.feature.booking.domain.model.BookingStatus
import com.example.m3showcase.feature.booking.domain.model.CancellationPolicy
import com.example.m3showcase.feature.booking.domain.model.Listing
import com.example.m3showcase.feature.booking.domain.model.ListingCategory
import com.example.m3showcase.feature.booking.domain.model.McpConnectionState
import com.example.m3showcase.feature.booking.domain.model.McpRawToolResult
import com.example.m3showcase.feature.booking.domain.model.McpServerInfo
import com.example.m3showcase.feature.booking.domain.model.McpTool
import com.example.m3showcase.feature.booking.domain.model.McpToolParam
import com.example.m3showcase.feature.booking.domain.model.McpTrafficEntry
import com.example.m3showcase.feature.booking.domain.model.PriceUnit
import com.example.m3showcase.feature.booking.domain.repository.BookingRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.io.IOException

/**
 * Talks to the Booking MCP server through [client]. The repository does the MCP handshake
 * lazily, turns domain calls into tools/call requests, and maps structuredContent back
 * into domain models.
 */
class BookingRepositoryImpl(
    private val client: McpClient,
    private val assets: AssetReader,
    private val dispatchers: DispatcherProvider,
    appScope: CoroutineScope,
) : BookingRepository {

    private val _connection = MutableStateFlow<McpConnectionState>(McpConnectionState.Idle)
    override val connection: StateFlow<McpConnectionState> = _connection.asStateFlow()

    override val traffic: StateFlow<List<McpTrafficEntry>> = client.log
        .map { entries -> entries.map { McpTrafficEntry(it.id, it.outgoing, it.method, it.body, it.timestamp, it.isError) } }
        .stateIn(appScope, SharingStarted.Eagerly, emptyList())

    private val _bookingsVersion = MutableStateFlow(0)
    override val bookingsVersion: StateFlow<Int> = _bookingsVersion.asStateFlow()

    private val connectMutex = Mutex()

    override suspend fun connect(reconnect: Boolean): Result<Unit> = mcpCall { ensureConnected(reconnect) }

    private suspend fun ensureConnected(reconnect: Boolean = false) = connectMutex.withLock {
        if (!reconnect && _connection.value is McpConnectionState.Connected) return@withLock
        _connection.value = McpConnectionState.Connecting
        try {
            val entry = withContext(dispatchers.io) { McpConfig.parse(assets.read(McpConfig.Path)).server(ServerKey) }
            val handshake = client.initialize()
            val tools = client.listTools().map { it.toDomain() }
            _connection.value = McpConnectionState.Connected(
                server = McpServerInfo(
                    displayName = entry.displayName,
                    name = handshake.serverName,
                    title = handshake.serverTitle,
                    version = handshake.serverVersion,
                    protocolVersion = handshake.protocolVersion,
                    transport = entry.transport,
                    configPath = "assets/${McpConfig.Path}",
                    definitionPath = "assets/${entry.definition}",
                    instructions = handshake.instructions,
                ),
                tools = tools,
            )
        } catch (e: CancellationException) {
            _connection.value = McpConnectionState.Idle
            throw e
        } catch (e: Exception) {
            _connection.value = McpConnectionState.Failed(e.toDomainError())
            throw e
        }
    }

    override suspend fun searchListings(query: String, category: ListingCategory?): Result<List<Listing>> =
        tool("search_listings", ListingsResult.serializer(), buildJsonObject {
            if (query.isNotBlank()) put("query", query.trim())
            category?.let { put("category", it.name.uppercase()) }
        }).map { result -> result.listings.mapNotNull { it.toDomainOrNull() } }

    override suspend fun getListing(id: String): Result<Listing> =
        tool("get_listing", ListingResult.serializer(), buildJsonObject { put("listingId", id) })
            .map { it.listing.toDomain() }

    override suspend fun checkAvailability(listingId: String, checkIn: CalendarDate, nights: Int, guests: Int): Result<BookingQuote> =
        tool("check_availability", QuoteResult.serializer(), buildJsonObject {
            put("listingId", listingId)
            put("checkIn", checkIn.toIsoString())
            put("nights", nights)
            put("guests", guests)
        }).map { it.quote.toDomain() }

    override suspend fun createBooking(request: BookingRequest): Result<Booking> =
        tool("create_booking", BookingResult.serializer(), buildJsonObject {
            put("listingId", request.listingId)
            put("checkIn", request.checkIn.toIsoString())
            put("nights", request.nights)
            put("guests", request.guests)
            put("guestName", request.guestName)
            if (request.note.isNotBlank()) put("note", request.note)
        }).map { it.booking.toDomain() }.onSuccess { _bookingsVersion.update { it + 1 } }

    override suspend fun getBookings(): Result<List<Booking>> =
        tool("list_bookings", BookingsResult.serializer(), JsonObject(emptyMap()))
            .map { result -> result.bookings.map { it.toDomain() } }

    override suspend fun getBooking(id: String): Result<Booking> =
        tool("get_booking", BookingResult.serializer(), buildJsonObject { put("bookingId", id) })
            .map { it.booking.toDomain() }

    override suspend fun cancelBooking(id: String): Result<Booking> =
        tool("cancel_booking", BookingResult.serializer(), buildJsonObject { put("bookingId", id) })
            .map { it.booking.toDomain() }.onSuccess { _bookingsVersion.update { it + 1 } }

    override suspend fun runTool(name: String, argumentsJson: String): Result<McpRawToolResult> {
        val arguments = try {
            if (argumentsJson.isBlank()) JsonObject(emptyMap()) else McpJson.parseToJsonElement(argumentsJson).jsonObject
        } catch (e: IllegalArgumentException) {
            // Also covers SerializationException, and valid JSON that is not an object.
            return Result.failure(BookingError(BookingError.Code.InvalidArgument, "Arguments must be a JSON object"))
        }
        return mcpCall {
            ensureConnected()
            val started = System.currentTimeMillis()
            val result = client.callTool(name, arguments)
            val body = result.structured?.let { McpPrettyJson.encodeToString(JsonElement.serializer(), it) } ?: result.text
            // The inspector can create and cancel bookings too, so lists should reload.
            if (!result.isError && name in MutatingTools) _bookingsVersion.update { it + 1 }
            McpRawToolResult(name, result.isError, body, System.currentTimeMillis() - started)
        }
    }

    override fun clearTraffic() = client.clearLog()

    /** Connects if needed, calls [name], and decodes structuredContent with [deserializer]. */
    private suspend fun <T> tool(name: String, deserializer: DeserializationStrategy<T>, arguments: JsonObject): Result<T> = mcpCall {
        ensureConnected()
        val result = client.callTool(name, arguments)
        val structured = result.structured ?: throw BookingError(BookingError.Code.Unknown, "No structuredContent from $name")
        if (result.isError) {
            val error = runCatching { McpJson.decodeFromJsonElement(ToolErrorResult.serializer(), structured).error }.getOrNull()
            throw BookingError(error?.code.toErrorCode(), error?.message ?: result.text)
        }
        McpJson.decodeFromJsonElement(deserializer, structured)
    }

    /** Like safeApiCall, but keeps [BookingError] instead of flattening it into BankError.Unknown. */
    private suspend fun <T> mcpCall(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e.toDomainError())
    }

    private fun Throwable.toDomainError(): Throwable = when (this) {
        is BookingError, is BankError -> this
        is IOException -> BankError.NoInternet
        is McpProtocolException -> BookingError(BookingError.Code.Unknown, "MCP error $code: $message")
        is SerializationException -> BookingError(BookingError.Code.Unknown, "Unexpected reply: $message")
        else -> BankError.Unknown(message)
    }

    private companion object {
        const val ServerKey = "booking"
        val MutatingTools = setOf("create_booking", "cancel_booking")
    }
}

private fun String?.toErrorCode(): BookingError.Code = when (this) {
    "NOT_FOUND" -> BookingError.Code.NotFound
    "NOT_AVAILABLE" -> BookingError.Code.NotAvailable
    "TOO_MANY_GUESTS" -> BookingError.Code.TooManyGuests
    "PAST_DATE" -> BookingError.Code.PastDate
    "NOT_CANCELLABLE" -> BookingError.Code.NotCancellable
    "INVALID_ARGUMENT" -> BookingError.Code.InvalidArgument
    else -> BookingError.Code.Unknown
}

// region Mappers

private fun usd(minor: Long, currency: String?) = Money(minor, CurrencyCode.entries.firstOrNull { it.name == currency } ?: CurrencyCode.USD)

private fun ListingDto.toDomainOrNull(): Listing? = runCatching { toDomain() }.getOrNull()

private fun ListingDto.toDomain(): Listing = Listing(
    id = id,
    name = name,
    category = ListingCategory.entries.first { it.name.equals(category, ignoreCase = true) },
    city = city,
    address = address,
    rating = rating,
    reviewCount = reviewCount,
    price = usd(priceMinor, "USD"),
    unit = if (unit == "PERSON") PriceUnit.Person else PriceUnit.Night,
    maxGuests = maxGuests,
    cancellation = if (cancellation == "NON_REFUNDABLE") CancellationPolicy.NonRefundable else CancellationPolicy.Free48h,
    description = description,
    // Unknown amenities from a newer server are skipped, not fatal.
    amenities = amenities.mapNotNull { code -> Amenity.entries.firstOrNull { it.name.equals(code.replace("_", ""), ignoreCase = true) } },
)

private fun QuoteDto.toDomain() = BookingQuote(
    checkIn = parseIsoDate(checkIn),
    checkOut = parseIsoDate(checkOut),
    nights = nights,
    guests = guests,
    unitPrice = usd(unitPriceMinor, currency),
    subtotal = usd(subtotalMinor, currency),
    tax = usd(taxMinor, currency),
    total = usd(totalMinor, currency),
    available = available,
    unavailableDate = unavailableDate?.let(::parseIsoDate),
)

private fun BookingDto.toDomain(): Booking {
    val listing = listing?.toDomain() ?: throw BookingError(BookingError.Code.Unknown, "Booking $id came without its listing")
    return Booking(
        id = id,
        listing = listing,
        checkIn = parseIsoDate(checkIn),
        checkOut = parseIsoDate(checkOut ?: checkIn),
        nights = nights,
        guests = guests,
        guestName = guestName,
        note = note,
        status = when (status) {
            "CANCELLED" -> BookingStatus.Cancelled
            "COMPLETED" -> BookingStatus.Completed
            else -> BookingStatus.Confirmed
        },
        confirmationCode = confirmationCode,
        subtotal = usd(subtotalMinor ?: 0, currency),
        tax = usd(taxMinor ?: 0, currency),
        total = usd(totalMinor ?: 0, currency),
        refund = refundMinor?.let { usd(it, currency) },
        bookedOn = parseIsoDate(createdAt),
    )
}

private fun McpToolDescriptor.toDomain(): McpTool {
    val properties = inputSchema["properties"] as? JsonObject ?: JsonObject(emptyMap())
    val required = (inputSchema["required"]?.jsonArray?.mapNotNull { (it as? JsonPrimitive)?.contentOrNull } ?: emptyList()).toSet()
    return McpTool(
        name = name,
        title = title,
        description = description,
        params = properties.map { (key, schema) ->
            val fields = schema.jsonObject
            McpToolParam(
                name = key,
                type = fields["format"]?.jsonPrimitive?.contentOrNull ?: fields["type"]?.jsonPrimitive?.contentOrNull ?: "any",
                required = key in required,
                description = fields["description"]?.jsonPrimitive?.contentOrNull,
                options = fields["enum"]?.jsonArray?.mapNotNull { (it as? JsonPrimitive)?.contentOrNull } ?: emptyList(),
            )
        },
        exampleArguments = McpPrettyJson.encodeToString(JsonElement.serializer(), example ?: JsonObject(emptyMap())),
    )
}

// endregion
