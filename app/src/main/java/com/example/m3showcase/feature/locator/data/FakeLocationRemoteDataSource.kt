package com.example.m3showcase.feature.locator.data

import com.example.m3showcase.core.data.remote.FakeNetwork

data class LocationDto(
    val id: String,
    val name: String,
    val type: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val phone: String?,
    /** "24H", or "HH:MM-HH:MM" per day group; empty = closed. */
    val weekdays: String,
    val saturday: String,
    val sunday: String,
)

/** Stand-in for `GET /locations`. Real Phnom Penh landmarks, made-up branches. */
class FakeLocationRemoteDataSource(private val network: FakeNetwork) {

    suspend fun getLocations(): List<LocationDto> = network.call(latencyMs = 600) { Locations }

    private companion object {
        val Locations = listOf(
            LocationDto("br-01", "Nova Bank Head Office", "BRANCH", "No. 20, Preah Norodom Blvd, Daun Penh", 11.5696, 104.9230, "023 000 000", "08:00-16:00", "08:00-11:30", ""),
            LocationDto("br-02", "Nova Bank BKK1", "BRANCH", "St. 278, Boeung Keng Kang I, Chamkar Mon", 11.5530, 104.9255, "023 000 101", "08:00-16:00", "08:00-11:30", ""),
            LocationDto("br-03", "Nova Bank Toul Kork", "BRANCH", "Russian Federation Blvd, Toul Kork", 11.5720, 104.8960, "023 000 102", "08:00-16:00", "08:00-11:30", ""),
            LocationDto("br-04", "Nova Bank Aeon Mall 1", "BRANCH", "Samdach Sothearos Blvd, Tonle Bassac", 11.5487, 104.9330, "023 000 103", "09:00-20:00", "09:00-20:00", "09:00-20:00"),
            LocationDto("br-05", "Nova Bank Chbar Ampov", "BRANCH", "National Road 1, Chbar Ampov", 11.5390, 104.9470, "023 000 104", "08:00-16:00", "", ""),
            LocationDto("br-06", "Nova Bank Sen Sok", "BRANCH", "Aeon Mall Sen Sok City, St. 1003", 11.6025, 104.8885, "023 000 105", "09:00-20:00", "09:00-20:00", "09:00-20:00"),
            LocationDto("atm-01", "ATM · Independence Monument", "ATM", "Preah Norodom Blvd near Independence Monument", 11.5563, 104.9283, null, "24H", "24H", "24H"),
            LocationDto("atm-02", "ATM · Central Market", "ATM", "Phsar Thmey, Daun Penh", 11.5696, 104.9210, null, "24H", "24H", "24H"),
            LocationDto("atm-03", "ATM · Riverside", "ATM", "Preah Sisowath Quay, Daun Penh", 11.5715, 104.9290, null, "24H", "24H", "24H"),
            LocationDto("atm-04", "ATM · Russian Market", "ATM", "St. 155, Toul Tom Poung", 11.5392, 104.9175, null, "24H", "24H", "24H"),
            LocationDto("atm-05", "ATM · Olympic Stadium", "ATM", "Charles de Gaulle Blvd, 7 Makara", 11.5585, 104.9118, null, "24H", "24H", "24H"),
            LocationDto("cdm-01", "Cash Deposit · Aeon Mall 1", "CASH_DEPOSIT", "Samdach Sothearos Blvd, Tonle Bassac", 11.5489, 104.9333, null, "09:00-22:00", "09:00-22:00", "09:00-22:00"),
            LocationDto("cdm-02", "Cash Deposit · Toul Kork", "CASH_DEPOSIT", "Russian Federation Blvd, Toul Kork", 11.5722, 104.8963, null, "06:00-22:00", "06:00-22:00", "06:00-22:00"),
        )
    }
}
