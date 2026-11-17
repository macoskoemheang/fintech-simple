package com.example.m3showcase.feature.booking.domain.repository

import com.example.m3showcase.core.domain.model.CalendarDate
import com.example.m3showcase.feature.booking.domain.model.Booking
import com.example.m3showcase.feature.booking.domain.model.BookingQuote
import com.example.m3showcase.feature.booking.domain.model.BookingRequest
import com.example.m3showcase.feature.booking.domain.model.Listing
import com.example.m3showcase.feature.booking.domain.model.ListingCategory
import com.example.m3showcase.feature.booking.domain.model.McpConnectionState
import com.example.m3showcase.feature.booking.domain.model.McpRawToolResult
import com.example.m3showcase.feature.booking.domain.model.McpTrafficEntry
import kotlinx.coroutines.flow.StateFlow

/** Bookings, served by the Booking MCP server. Every call connects first if needed. */
interface BookingRepository {
    val connection: StateFlow<McpConnectionState>
    val traffic: StateFlow<List<McpTrafficEntry>>
    /** Goes up by one whenever a booking is created or cancelled, so lists know to reload. */
    val bookingsVersion: StateFlow<Int>

    suspend fun connect(reconnect: Boolean = false): Result<Unit>
    suspend fun searchListings(query: String, category: ListingCategory?): Result<List<Listing>>
    suspend fun getListing(id: String): Result<Listing>
    suspend fun checkAvailability(listingId: String, checkIn: CalendarDate, nights: Int, guests: Int): Result<BookingQuote>
    suspend fun createBooking(request: BookingRequest): Result<Booking>
    suspend fun getBookings(): Result<List<Booking>>
    suspend fun getBooking(id: String): Result<Booking>
    suspend fun cancelBooking(id: String): Result<Booking>

    /** Calls any tool with raw JSON arguments and returns the raw reply, for the inspector. */
    suspend fun runTool(name: String, argumentsJson: String): Result<McpRawToolResult>
    fun clearTraffic()
}
