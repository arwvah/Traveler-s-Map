package com.travelersmap.domain.repository

import com.travelersmap.domain.model.AppSettings
import com.travelersmap.domain.model.BudgetBreakdown
import com.travelersmap.domain.model.BudgetInput
import com.travelersmap.domain.model.RouteEstimate
import com.travelersmap.domain.model.TouristPlace
import com.travelersmap.domain.model.TravelMode
import com.travelersmap.domain.model.WeatherInfo
import kotlinx.coroutines.flow.Flow

interface PlaceRepository {
    fun observePlaces(countryCode: String = "UZ"): Flow<List<TouristPlace>>
    fun observePlace(id: String): Flow<TouristPlace?>
    fun search(query: String, countryCode: String = "UZ"): Flow<List<TouristPlace>>
    suspend fun getPlace(id: String): TouristPlace?
    suspend fun getAll(countryCode: String = "UZ"): List<TouristPlace>
    suspend fun markViewed(placeId: String)
    fun observeRecentlyViewed(limit: Int = 20): Flow<List<TouristPlace>>
}

interface FavoriteRepository {
    fun observeFavorites(): Flow<List<TouristPlace>>
    fun observeFavoriteIds(): Flow<Set<String>>
    suspend fun toggle(placeId: String)
    suspend fun isFavorite(placeId: String): Boolean
}

interface RouteRepository {
    suspend fun route(
        fromLat: Double,
        fromLng: Double,
        toLat: Double,
        toLng: Double,
        mode: TravelMode
    ): RouteEstimate

    /** Offline haversine estimate (sync) for quick UI previews. */
    fun estimate(
        fromLat: Double,
        fromLng: Double,
        toLat: Double,
        toLng: Double,
        mode: TravelMode
    ): RouteEstimate
}

interface BudgetRepository {
    fun estimate(input: BudgetInput): BudgetBreakdown
}

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun setDarkMode(enabled: Boolean)
    suspend fun setLanguage(language: com.travelersmap.domain.model.AppLanguage)
}

interface WeatherRepository {
    suspend fun weatherFor(lat: Double, lng: Double): WeatherInfo
}
