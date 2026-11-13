package com.example.m3showcase.feature.locator.data

import com.example.m3showcase.core.data.remote.safeApiCall
import com.example.m3showcase.feature.locator.domain.model.BankLocation
import com.example.m3showcase.feature.locator.domain.model.GeoPoint
import com.example.m3showcase.feature.locator.domain.model.LocationType
import com.example.m3showcase.feature.locator.domain.model.OpeningHours
import com.example.m3showcase.feature.locator.domain.model.TimeRange
import com.example.m3showcase.feature.locator.domain.repository.LocationRepository

class LocationRepositoryImpl(private val remote: FakeLocationRemoteDataSource) : LocationRepository {

    override suspend fun getLocations(): Result<List<BankLocation>> = safeApiCall { remote.getLocations().map { it.toDomain() } }

    /** Independence Monument, Phnom Penh. */
    override suspend fun currentPosition(): GeoPoint = GeoPoint(11.5564, 104.9282)
}

private fun LocationDto.toDomain() = BankLocation(
    id = id,
    name = name,
    type = when (type) { "ATM" -> LocationType.Atm; "CASH_DEPOSIT" -> LocationType.CashDeposit; else -> LocationType.Branch },
    address = address,
    point = GeoPoint(lat, lng),
    phone = phone,
    hours = if (weekdays == "24H") OpeningHours(is24Hours = true)
    else OpeningHours(weekdays = weekdays.toRange(), saturday = saturday.toRange(), sunday = sunday.toRange()),
)

/** "08:00-16:30" → TimeRange(480, 990); "" → closed. */
internal fun String.toRange(): TimeRange? {
    if (isBlank()) return null
    fun minutes(t: String) = t.split(':').let { (h, m) -> h.toInt() * 60 + m.toInt() }
    val (open, close) = split('-')
    return TimeRange(minutes(open), minutes(close))
}
