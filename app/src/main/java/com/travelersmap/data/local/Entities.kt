package com.travelersmap.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tourist_places")
data class TouristPlaceEntity(
    @PrimaryKey val id: String,
    val countryCode: String,
    val name: String,
    val city: String,
    val region: String,
    val category: String,
    val shortDescription: String,
    val description: String,
    val history: String,
    val latitude: Double,
    val longitude: Double,
    val photoUrlsCsv: String,
    val openingHours: String,
    val ticketPrice: String,
    val estimatedVisitMinutes: Int,
    val bestSeason: String,
    val difficulty: String,
    val familyFriendly: Boolean,
    val rating: Float,
    val nearbyIdsCsv: String,
    val website: String?,
    val phone: String?,
    val accentColorHex: Long
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val placeId: String,
    val savedAt: Long
)

@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey val query: String,
    val searchedAt: Long
)

@Entity(tableName = "recently_viewed")
data class RecentlyViewedEntity(
    @PrimaryKey val placeId: String,
    val viewedAt: Long
)

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey val cacheKey: String,
    val json: String,
    val fetchedAtMs: Long
)
