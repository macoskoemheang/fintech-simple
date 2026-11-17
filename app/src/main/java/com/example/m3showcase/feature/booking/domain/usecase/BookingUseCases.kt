package com.example.m3showcase.feature.booking.domain.usecase

import com.example.m3showcase.core.domain.model.CalendarDate
import com.example.m3showcase.feature.booking.domain.model.Booking
import com.example.m3showcase.feature.booking.domain.model.BookingRequest
import com.example.m3showcase.feature.booking.domain.model.BookingStatus
import com.example.m3showcase.feature.booking.domain.model.ListingCategory
import com.example.m3showcase.feature.booking.domain.repository.BookingRepository

class ObserveMcpConnectionUseCase(private val repository: BookingRepository) {
    operator fun invoke() = repository.connection
}

class ConnectBookingServerUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(reconnect: Boolean = false) = repository.connect(reconnect)
}

class SearchListingsUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(query: String, category: ListingCategory?) = repository.searchListings(query, category)
}

class GetListingUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(id: String) = repository.getListing(id)
}

class CheckAvailabilityUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(listingId: String, checkIn: CalendarDate, nights: Int, guests: Int) =
        repository.checkAvailability(listingId, checkIn, nights, guests)
}

class CreateBookingUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(request: BookingRequest) = repository.createBooking(request.copy(guestName = request.guestName.trim(), note = request.note.trim()))
}

/** Bookings split into what is coming up (soonest first) and the rest (newest first). */
data class BookingGroups(val upcoming: List<Booking>, val past: List<Booking>)

class GetBookingsUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(): Result<BookingGroups> = repository.getBookings().map { all ->
        val (upcoming, past) = all.partition { it.status == BookingStatus.Confirmed }
        BookingGroups(upcoming.sortedBy { it.checkIn }, past.sortedByDescending { it.checkIn })
    }
}

class ObserveBookingsVersionUseCase(private val repository: BookingRepository) {
    operator fun invoke() = repository.bookingsVersion
}

class GetBookingUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(id: String) = repository.getBooking(id)
}

class CancelBookingUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(id: String) = repository.cancelBooking(id)
}

class ObserveMcpTrafficUseCase(private val repository: BookingRepository) {
    operator fun invoke() = repository.traffic
}

class RunMcpToolUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(name: String, argumentsJson: String) = repository.runTool(name, argumentsJson)
}

class ClearMcpTrafficUseCase(private val repository: BookingRepository) {
    operator fun invoke() = repository.clearTraffic()
}
