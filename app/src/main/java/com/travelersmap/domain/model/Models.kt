package com.travelersmap.domain.model

/**
 * Country-scoped tourism models.
 * New countries = new dataset rows + CountryConfig — core stays the same.
 */
data class CountryConfig(
    val code: String,
    val name: String,
    val centerLat: Double,
    val centerLng: Double,
    val defaultZoom: Float
) {
    companion object {
        /** MVP is Uzbekistan-only. */
        val Uzbekistan = CountryConfig(
            code = "UZ",
            name = "Uzbekistan",
            centerLat = 41.3775,
            centerLng = 64.5853,
            defaultZoom = 5.8f
        )
    }
}

enum class PlaceCategory {
    HISTORICAL,
    MONUMENT,
    MUSEUM,
    MOSQUE,
    MADRASAH,
    FORTRESS,
    PALACE,
    UNESCO,
    MOUNTAIN,
    LAKE,
    WATERFALL,
    NATIONAL_PARK,
    NATURE_RESERVE,
    ANCIENT_CITY,
    VIEWPOINT
}

enum class Difficulty { EASY, MODERATE, HARD }

data class TouristPlace(
    val id: String,
    val countryCode: String,
    val name: String,
    val city: String,
    val category: PlaceCategory,
    val shortDescription: String,
    val description: String,
    val history: String,
    val latitude: Double,
    val longitude: Double,
    val photoUrls: List<String>,
    val openingHours: String,
    val ticketPrice: String,
    val estimatedVisitMinutes: Int,
    val bestSeason: String,
    val difficulty: Difficulty,
    val familyFriendly: Boolean,
    val rating: Float,
    val nearbyIds: List<String>,
    val accentColorHex: Long = 0xFFC9A227 // muted gold accent for tourist pins only
)

data class FavoritePlace(
    val placeId: String,
    val savedAt: Long
)

enum class TravelMode { WALKING, DRIVING, CYCLING, TRANSIT }

data class RouteEstimate(
    val mode: TravelMode,
    val distanceKm: Double,
    val durationMinutes: Int,
    val isPlaceholder: Boolean = false
)

data class AiItineraryDay(
    val dayNumber: Int,
    val title: String,
    val placeIds: List<String>,
    val notes: String
)

data class AiItinerary(
    val title: String,
    val summary: String,
    val days: List<AiItineraryDay>
)

data class BudgetInput(
    val totalBudgetUsd: Double,
    val days: Int,
    val travelers: Int
)

data class BudgetBreakdown(
    val hotels: Double,
    val food: Double,
    val transportation: Double,
    val tickets: Double,
    val dailyBudget: Double,
    val remaining: Double
)

enum class AppLanguage { ENGLISH, UZBEK, RUSSIAN }

data class AppSettings(
    val darkMode: Boolean = true,
    val language: AppLanguage = AppLanguage.ENGLISH
)
