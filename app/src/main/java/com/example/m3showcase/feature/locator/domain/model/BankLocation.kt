package com.example.m3showcase.feature.locator.domain.model

data class GeoPoint(val latitude: Double, val longitude: Double)

/** Minutes since midnight, [open] inclusive, [close] exclusive. */
data class TimeRange(val open: Int, val close: Int) {
    operator fun contains(minute: Int): Boolean = minute in open until close
}

/** Weekly hours. A null range means closed that day; [is24Hours] overrides everything. */
data class OpeningHours(
    val is24Hours: Boolean = false,
    val weekdays: TimeRange? = null,
    val saturday: TimeRange? = null,
    val sunday: TimeRange? = null,
) {
    /** [dayOfWeek]: 1 = Monday … 7 = Sunday. */
    fun rangeOn(dayOfWeek: Int): TimeRange? = when {
        is24Hours -> TimeRange(0, 24 * 60)
        dayOfWeek in 1..5 -> weekdays
        dayOfWeek == 6 -> saturday
        else -> sunday
    }
}

enum class LocationType { Branch, Atm, CashDeposit }

data class BankLocation(
    val id: String,
    val name: String,
    val type: LocationType,
    val address: String,
    val point: GeoPoint,
    val phone: String?,
    val hours: OpeningHours,
)

/** A location as seen from where the user is, right now. */
data class NearbyLocation(
    val location: BankLocation,
    val distanceKm: Double,
    val isOpenNow: Boolean,
    /** Today's hours; null when closed all day. */
    val todayHours: TimeRange?,
)
