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
    MAUSOLEUM,
    FORTRESS,
    PALACE,
    UNESCO,
    MOUNTAIN,
    LAKE,
    WATERFALL,
    NATIONAL_PARK,
    NATURE_RESERVE,
    ANCIENT_CITY,
    VIEWPOINT,
    BAZAAR,
    CRAFT_CENTER,
    SILK_WORKSHOP,
    TRADITIONAL_VILLAGE,
    CULTURAL_COMPLEX,
    THEME_PARK,
    BOTANICAL_GARDEN,
    ZOO,
    PARK,
    PILGRIMAGE,
    SACRED_SPRING,
    ARCHAEOLOGICAL,
    RIVER,
    CANYON,
    CAVE,
    FOREST
}

enum class Difficulty { EASY, MODERATE, HARD }

data class TouristPlace(
    val id: String,
    val countryCode: String,
    val name: String,
    val city: String,
    val region: String = city,
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
    val website: String? = null,
    val phone: String? = null,
    val accentColorHex: Long = 0xFFC9A227
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
    val isPlaceholder: Boolean = false,
    val polyline: List<LatLngPoint> = emptyList()
)

data class LatLngPoint(val latitude: Double, val longitude: Double)

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float? = null
)

data class WeatherInfo(
    val temperatureC: Double,
    val feelsLikeC: Double,
    val humidityPercent: Int,
    val windSpeedKmh: Double,
    val condition: String,
    val conditionCode: Int,
    val todayHighC: Double,
    val todayLowC: Double,
    val todaySummary: String,
    val fetchedAtMs: Long = System.currentTimeMillis()
)

data class AiChatMessage(
    val id: String,
    val role: AiMessageRole,
    val content: String,
    val timestampMs: Long = System.currentTimeMillis()
)

enum class AiMessageRole { USER, ASSISTANT, SYSTEM }

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
