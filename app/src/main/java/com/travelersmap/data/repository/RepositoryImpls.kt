package com.travelersmap.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.travelersmap.data.local.FavoriteDao
import com.travelersmap.data.local.FavoriteEntity
import com.travelersmap.data.local.PlaceDao
import com.travelersmap.data.local.RecentlyViewedDao
import com.travelersmap.data.local.RecentlyViewedEntity
import com.travelersmap.data.local.WeatherCacheDao
import com.travelersmap.data.local.WeatherCacheEntity
import com.travelersmap.data.local.toDomain
import com.travelersmap.data.remote.DirectionsRemoteDataSource
import com.travelersmap.data.remote.WeatherRemoteDataSource
import com.travelersmap.domain.model.AppLanguage
import com.travelersmap.domain.model.AppSettings
import com.travelersmap.domain.model.BudgetBreakdown
import com.travelersmap.domain.model.BudgetInput
import com.travelersmap.domain.model.RouteEstimate
import com.travelersmap.domain.model.TouristPlace
import com.travelersmap.domain.model.TravelMode
import com.travelersmap.domain.model.WeatherInfo
import com.travelersmap.domain.repository.BudgetRepository
import com.travelersmap.domain.repository.FavoriteRepository
import com.travelersmap.domain.repository.PlaceRepository
import com.travelersmap.domain.repository.RouteRepository
import com.travelersmap.domain.repository.SettingsRepository
import com.travelersmap.domain.repository.WeatherRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaceRepositoryImpl @Inject constructor(
    private val placeDao: PlaceDao,
    private val recentlyViewedDao: RecentlyViewedDao
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

    override suspend fun markViewed(placeId: String) {
        recentlyViewedDao.insert(
            RecentlyViewedEntity(placeId = placeId, viewedAt = System.currentTimeMillis())
        )
    }

    override fun observeRecentlyViewed(limit: Int): Flow<List<TouristPlace>> =
        combine(
            recentlyViewedDao.observeRecent(limit),
            placeDao.observeByCountry("UZ")
        ) { recent, places ->
            val byId = places.associateBy { it.id }
            recent.mapNotNull { row -> byId[row.placeId]?.toDomain() }
        }
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
class RouteRepositoryImpl @Inject constructor(
    private val directions: DirectionsRemoteDataSource,
    private val mapsApiKey: String
) : RouteRepository {

    override suspend fun route(
        fromLat: Double,
        fromLng: Double,
        toLat: Double,
        toLng: Double,
        mode: TravelMode
    ): RouteEstimate = withContext(Dispatchers.IO) {
        directions.route(fromLat, fromLng, toLat, toLng, mode, mapsApiKey)
    }

    override fun estimate(
        fromLat: Double,
        fromLng: Double,
        toLat: Double,
        toLng: Double,
        mode: TravelMode
    ): RouteEstimate = directions.offlineEstimate(fromLat, fromLng, toLat, toLng, mode)
}

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val remote: WeatherRemoteDataSource,
    private val cacheDao: WeatherCacheDao
) : WeatherRepository {

    private val ttlMs = 20 * 60 * 1000L

    override suspend fun weatherFor(lat: Double, lng: Double): WeatherInfo =
        withContext(Dispatchers.IO) {
            val key = "%.2f,%.2f".format(lat, lng)
            val cached = cacheDao.get(key)
            val now = System.currentTimeMillis()
            if (cached != null && now - cached.fetchedAtMs < ttlMs) {
                runCatching { decode(cached.json, cached.fetchedAtMs) }.getOrNull()?.let { return@withContext it }
            }
            val fresh = remote.fetch(lat, lng)
            cacheDao.upsert(
                WeatherCacheEntity(
                    cacheKey = key,
                    json = encode(fresh),
                    fetchedAtMs = fresh.fetchedAtMs
                )
            )
            fresh
        }

    private fun encode(w: WeatherInfo): String = JSONObject().apply {
        put("temperatureC", w.temperatureC)
        put("feelsLikeC", w.feelsLikeC)
        put("humidityPercent", w.humidityPercent)
        put("windSpeedKmh", w.windSpeedKmh)
        put("condition", w.condition)
        put("conditionCode", w.conditionCode)
        put("todayHighC", w.todayHighC)
        put("todayLowC", w.todayLowC)
        put("todaySummary", w.todaySummary)
    }.toString()

    private fun decode(json: String, fetchedAt: Long): WeatherInfo {
        val o = JSONObject(json)
        return WeatherInfo(
            temperatureC = o.getDouble("temperatureC"),
            feelsLikeC = o.getDouble("feelsLikeC"),
            humidityPercent = o.getInt("humidityPercent"),
            windSpeedKmh = o.getDouble("windSpeedKmh"),
            condition = o.getString("condition"),
            conditionCode = o.getInt("conditionCode"),
            todayHighC = o.getDouble("todayHighC"),
            todayLowC = o.getDouble("todayLowC"),
            todaySummary = o.getString("todaySummary"),
            fetchedAtMs = fetchedAt
        )
    }
}

@Singleton
class BudgetRepositoryImpl @Inject constructor() : BudgetRepository {
    override fun estimate(input: BudgetInput): BudgetBreakdown {
        val days = input.days.coerceAtLeast(1)
        val people = input.travelers.coerceAtLeast(1)
        val hotels = 45.0 * days * ((people + 1) / 2)
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
