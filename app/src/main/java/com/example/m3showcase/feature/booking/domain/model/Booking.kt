package com.example.m3showcase.feature.booking.domain.model

import androidx.compose.runtime.Immutable
import com.example.m3showcase.core.domain.model.CalendarDate
import com.example.m3showcase.core.domain.model.Money

enum class ListingCategory { Hotel, Resort, Tour, Dining }

/** Stays are priced per night, tours and dining per person. */
enum class PriceUnit { Night, Person }

enum class CancellationPolicy { Free48h, NonRefundable }

enum class Amenity { Wifi, Breakfast, Pool, Gym, Spa, AirportTransfer, Bikes, AllInclusive, Beach, Guide, Lunch, SetMenu, Vegetarian, WinePairing }

@Immutable
data class Listing(
    val id: String,
    val name: String,
    val category: ListingCategory,
    val city: String,
    val address: String,
    val rating: Double,
    val reviewCount: Int,
    val price: Money,
    val unit: PriceUnit,
    val maxGuests: Int,
    val cancellation: CancellationPolicy,
    val description: String,
    val amenities: List<Amenity>,
)

@Immutable
data class BookingQuote(
    val checkIn: CalendarDate,
    val checkOut: CalendarDate,
    val nights: Int,
    val guests: Int,
    val unitPrice: Money,
    val subtotal: Money,
    val tax: Money,
    val total: Money,
    val available: Boolean,
    /** The first date that is taken, when [available] is false. */
    val unavailableDate: CalendarDate?,
)

enum class BookingStatus { Confirmed, Completed, Cancelled }

@Immutable
data class Booking(
    val id: String,
    val listing: Listing,
    val checkIn: CalendarDate,
    val checkOut: CalendarDate,
    val nights: Int,
    val guests: Int,
    val guestName: String,
    val note: String,
    val status: BookingStatus,
    val confirmationCode: String,
    val subtotal: Money,
    val tax: Money,
    val total: Money,
    /** Set once cancelled: what the user gets back (zero for non-refundable). */
    val refund: Money?,
    val bookedOn: CalendarDate,
)

data class BookingRequest(
    val listingId: String,
    val checkIn: CalendarDate,
    val nights: Int,
    val guests: Int,
    val guestName: String,
    val note: String,
)

/** A failure a booking tool reported (isError = true), by the server's error code. */
class BookingError(val code: Code, message: String) : Exception(message) {
    enum class Code { NotFound, NotAvailable, TooManyGuests, PastDate, NotCancellable, InvalidArgument, Unknown }
}
