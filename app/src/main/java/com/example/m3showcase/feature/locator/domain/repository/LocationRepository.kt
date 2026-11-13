package com.example.m3showcase.feature.locator.domain.repository

import com.example.m3showcase.feature.locator.domain.model.BankLocation
import com.example.m3showcase.feature.locator.domain.model.GeoPoint

interface LocationRepository {
    suspend fun getLocations(): Result<List<BankLocation>>

    /**
     * Where distances are measured from. A real app would ask FusedLocationProvider (with the
     * location permission); this demo uses a fixed point in central Phnom Penh.
     */
    suspend fun currentPosition(): GeoPoint
}
