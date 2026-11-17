package com.example.m3showcase.feature.booking.data.mcp

import kotlinx.serialization.Serializable

/*
 * The JSON shapes inside the Booking server's tool results (structuredContent),
 * and of the mockData block in assets/mcp/booking-server.json.
 * Money is in minor units, dates are ISO-8601 strings.
 */

@Serializable
data class ListingDto(
    val id: String,
    val name: String,
    val category: String,
    val city: String,
    val address: String,
    val rating: Double,
    val reviewCount: Int,
    val priceMinor: Long,
    val unit: String,
    val maxGuests: Int,
    val cancellation: String,
    val description: String,
    val amenities: List<String> = emptyList(),
    val blockedDates: List<String> = emptyList(),
)

@Serializable
data class QuoteDto(
    val listingId: String,
    val checkIn: String,
    val checkOut: String,
    val nights: Int,
    val guests: Int,
    val unitPriceMinor: Long,
    val subtotalMinor: Long,
    val taxMinor: Long,
    val totalMinor: Long,
    val currency: String,
    val available: Boolean,
    val unavailableDate: String? = null,
)

@Serializable
data class BookingDto(
    val id: String,
    val listingId: String,
    val checkIn: String,
    val nights: Int,
    val guests: Int,
    val guestName: String,
    val note: String = "",
    val status: String,
    val confirmationCode: String,
    val createdAt: String,
    val checkOut: String? = null,
    val subtotalMinor: Long? = null,
    val taxMinor: Long? = null,
    val totalMinor: Long? = null,
    val refundMinor: Long? = null,
    val currency: String? = null,
    /** Filled in by the server on the way out. */
    val listing: ListingDto? = null,
)

@Serializable
data class BookingMockData(
    val currency: String,
    val taxRateBasisPoints: Int,
    val listings: List<ListingDto>,
    val bookings: List<BookingDto>,
)

@Serializable data class ListingsResult(val listings: List<ListingDto>, val total: Int)
@Serializable data class ListingResult(val listing: ListingDto)
@Serializable data class QuoteResult(val quote: QuoteDto)
@Serializable data class BookingResult(val booking: BookingDto)
@Serializable data class BookingsResult(val bookings: List<BookingDto>)
@Serializable data class ToolErrorDto(val code: String, val message: String)
@Serializable data class ToolErrorResult(val error: ToolErrorDto)
