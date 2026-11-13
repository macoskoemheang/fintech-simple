package com.example.m3showcase.feature.services

import com.example.m3showcase.core.common.LocalMoment
import com.example.m3showcase.feature.locator.domain.model.BankLocation
import com.example.m3showcase.feature.locator.domain.model.GeoPoint
import com.example.m3showcase.feature.locator.domain.model.LocationType
import com.example.m3showcase.feature.locator.domain.model.OpeningHours
import com.example.m3showcase.feature.locator.domain.model.TimeRange
import com.example.m3showcase.feature.locator.domain.repository.LocationRepository
import com.example.m3showcase.feature.locator.domain.usecase.FilterLocationsUseCase
import com.example.m3showcase.feature.locator.domain.usecase.GetNearbyLocationsUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LocatorTest {
    private val here = GeoPoint(11.5564, 104.9282)
    private val branch = BankLocation("b", "Nova BKK1", LocationType.Branch, "St. 278", GeoPoint(11.5530, 104.9255), "023", OpeningHours(weekdays = TimeRange(480, 960), saturday = TimeRange(480, 690)))
    private val atm = BankLocation("a", "ATM Riverside", LocationType.Atm, "Sisowath Quay", GeoPoint(11.5715, 104.9290), null, OpeningHours(is24Hours = true))

    private val repo = object : LocationRepository {
        override suspend fun getLocations() = Result.success(listOf(atm, branch))
        override suspend fun currentPosition() = here
    }

    @Test fun `haversine distance is about right`() {
        // ~0.48 km between these two Phnom Penh points.
        assertEquals(0.48, GetNearbyLocationsUseCase.distanceKm(here, branch.point), 0.02)
    }

    @Test fun `nearest first, and open status follows the clock`() = runTest {
        val sundayNight = GetNearbyLocationsUseCase(repo) { LocalMoment(dayOfWeek = 7, minuteOfDay = 22 * 60) }().getOrThrow()
        assertEquals(listOf("b", "a"), sundayNight.map { it.location.id })
        assertEquals(listOf(false, true), sundayNight.map { it.isOpenNow })

        val mondayMorning = GetNearbyLocationsUseCase(repo) { LocalMoment(1, 9 * 60) }().getOrThrow()
        assertEquals(true, mondayMorning.first { it.location.id == "b" }.isOpenNow)
        val mondayAtClose = GetNearbyLocationsUseCase(repo) { LocalMoment(1, 16 * 60) }().getOrThrow()
        assertEquals(false, mondayAtClose.first { it.location.id == "b" }.isOpenNow) // closing time is exclusive
    }

    @Test fun `filters combine`() = runTest {
        val all = GetNearbyLocationsUseCase(repo) { LocalMoment(7, 22 * 60) }().getOrThrow()
        val filter = FilterLocationsUseCase()
        assertEquals(listOf("a"), filter(all, "", null, openNowOnly = true).map { it.location.id })
        assertEquals(listOf("b"), filter(all, "bkk", null, false).map { it.location.id })
        assertEquals(listOf("a"), filter(all, "", LocationType.Atm, false).map { it.location.id })
        assertEquals(emptyList<String>(), filter(all, "bkk", LocationType.Atm, false).map { it.location.id })
    }
}
