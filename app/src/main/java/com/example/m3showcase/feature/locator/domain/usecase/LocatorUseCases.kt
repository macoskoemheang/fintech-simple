package com.example.m3showcase.feature.locator.domain.usecase

import com.example.m3showcase.core.common.LocalTimeProvider
import com.example.m3showcase.feature.locator.domain.model.GeoPoint
import com.example.m3showcase.feature.locator.domain.model.LocationType
import com.example.m3showcase.feature.locator.domain.model.NearbyLocation
import com.example.m3showcase.feature.locator.domain.repository.LocationRepository
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** Every location with distance and "open now", nearest first. */
class GetNearbyLocationsUseCase(
    private val repository: LocationRepository,
    private val timeProvider: LocalTimeProvider,
) {
    suspend operator fun invoke(): Result<List<NearbyLocation>> = repository.getLocations().map { locations ->
        val here = repository.currentPosition()
        val now = timeProvider.now()
        locations.map { location ->
            val today = location.hours.rangeOn(now.dayOfWeek)
            NearbyLocation(
                location = location,
                distanceKm = distanceKm(here, location.point),
                isOpenNow = today?.contains(now.minuteOfDay) == true,
                todayHours = today,
            )
        }.sortedBy { it.distanceKm }
    }

    companion object {
        private const val EarthRadiusKm = 6371.0

        /** Great-circle (haversine) distance. */
        fun distanceKm(a: GeoPoint, b: GeoPoint): Double {
            val dLat = Math.toRadians(b.latitude - a.latitude)
            val dLon = Math.toRadians(b.longitude - a.longitude)
            val h = sin(dLat / 2).pow(2) + cos(Math.toRadians(a.latitude)) * cos(Math.toRadians(b.latitude)) * sin(dLon / 2).pow(2)
            return 2 * EarthRadiusKm * asin(sqrt(h))
        }
    }
}

/** Search + filters, applied on the device (the list is small). */
class FilterLocationsUseCase {
    operator fun invoke(all: List<NearbyLocation>, query: String, type: LocationType?, openNowOnly: Boolean): List<NearbyLocation> {
        val q = query.trim()
        return all.filter { item ->
            (type == null || item.location.type == type) &&
                (!openNowOnly || item.isOpenNow) &&
                (q.isEmpty() || item.location.name.contains(q, ignoreCase = true) || item.location.address.contains(q, ignoreCase = true))
        }
    }
}
