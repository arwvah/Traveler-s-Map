package com.travelersmap.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.travelersmap.data.local.FavoriteDao
import com.travelersmap.data.local.FavoriteEntity
import com.travelersmap.data.local.PlaceDao
import com.travelersmap.data.local.toDomain
import com.travelersmap.domain.model.AppLanguage
import com.travelersmap.domain.model.AppSettings
import com.travelersmap.domain.model.BudgetBreakdown
import com.travelersmap.domain.model.BudgetInput
import com.travelersmap.domain.model.RouteEstimate
import com.travelersmap.domain.model.TouristPlace
import com.travelersmap.domain.model.TravelMode
import com.travelersmap.domain.repository.BudgetRepository
import com.travelersmap.domain.repository.FavoriteRepository
import com.travelersmap.domain.repository.PlaceRepository
import com.travelersmap.domain.repository.RouteRepository
import com.travelersmap.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Singleton
class PlaceRepositoryImpl @Inject constructor(
    private val placeDao: PlaceDao
) : PlaceRepository {
    override fun observePlaces(countryCode: String): Flow<List<TouristPlace>> =
        placeDao.observeByCountry(countryCode).map { list -> list.map { it.toDomain() } }

    override fun observePlace(id: String): Flow<TouristPlace?> =
        placeDao.observeById(id).map { it?.toDomain() }

    override fun search(query: String, countryCode: String): Flow<List<TouristPlace>> {
        val q = query.trim()
        return if (q.isEmpty()) observePlaces(countryCode)
        else placeDao.search(q, countryCode).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getPlace(id: String): TouristPlace? =
        placeDao.getById(id)?.toDomain()

    override suspend fun getAll(countryCode: String): List<TouristPlace> =
        placeDao.getAll(countryCode).map { it.toDomain() }
}

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao,
    private val placeDao: PlaceDao
) : FavoriteRepository {
    override fun observeFavorites(): Flow<List<TouristPlace>> =
        combine(
            favoriteDao.observeAll(),
            placeDao.observeByCountry("UZ")
        ) { favs, places ->
            val byId = places.associateBy { it.id }
            favs.mapNotNull { fav -> byId[fav.placeId]?.toDomain() }
        }

    override fun observeFavoriteIds(): Flow<Set<String>> =
        favoriteDao.observeIds().map { it.toSet() }

    override suspend fun toggle(placeId: String) {
        if (favoriteDao.exists(placeId)) favoriteDao.delete(placeId)
        else favoriteDao.insert(FavoriteEntity(placeId, System.currentTimeMillis()))
    }

    override suspend fun isFavorite(placeId: String): Boolean = favoriteDao.exists(placeId)
}

@Singleton
class RouteRepositoryImpl @Inject constructor() : RouteRepository {
    override fun estimate(
        fromLat: Double,
        fromLng: Double,
        toLat: Double,
        toLng: Double,
        mode: TravelMode
    ): RouteEstimate {
        val km = haversineKm(fromLat, fromLng, toLat, toLng)
        // Rough road factor for MVP offline estimates
        val road = km * 1.25
        return when (mode) {
            TravelMode.WALKING -> RouteEstimate(mode, road, (road / 5.0 * 60).toInt().coerceAtLeast(1))
            TravelMode.DRIVING -> RouteEstimate(mode, road, (road / 60.0 * 60).toInt().coerceAtLeast(1))
            TravelMode.CYCLING -> RouteEstimate(mode, road, (road / 15.0 * 60).toInt().coerceAtLeast(1))
            TravelMode.TRANSIT -> RouteEstimate(
                mode,
                road,
                (road / 40.0 * 60).toInt().coerceAtLeast(5),
                isPlaceholder = true
            )
        }
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}

@Singleton
class BudgetRepositoryImpl @Inject constructor() : BudgetRepository {
    override fun estimate(input: BudgetInput): BudgetBreakdown {
        val days = input.days.coerceAtLeast(1)
        val people = input.travelers.coerceAtLeast(1)
        val hotels = 45.0 * days * ((people + 1) / 2) // shared rooms
        val food = 18.0 * days * people
        val transportation = 12.0 * days * people
        val tickets = 8.0 * days * people
        val spent = hotels + food + transportation + tickets
        val remaining = input.totalBudgetUsd - spent
        val daily = if (days > 0) input.totalBudgetUsd / days else 0.0
        return BudgetBreakdown(hotels, food, transportation, tickets, daily, remaining)
    }
}

private val Context.dataStore by preferencesDataStore("travelers_map_settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {
    private val darkKey = booleanPreferencesKey("dark_mode")
    private val langKey = stringPreferencesKey("language")

    override val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            darkMode = prefs[darkKey] ?: true,
            language = runCatching {
                AppLanguage.valueOf(prefs[langKey] ?: AppLanguage.ENGLISH.name)
            }.getOrDefault(AppLanguage.ENGLISH)
        )
    }

    override suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[darkKey] = enabled }
    }

    override suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[langKey] = language.name }
    }

    suspend fun snapshot(): AppSettings = settings.first()
}
