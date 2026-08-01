package com.travelersmap.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {
    @Query("SELECT * FROM tourist_places WHERE countryCode = :countryCode ORDER BY name ASC")
    fun observeByCountry(countryCode: String): Flow<List<TouristPlaceEntity>>

    @Query("SELECT * FROM tourist_places WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<TouristPlaceEntity?>

    @Query("SELECT * FROM tourist_places WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TouristPlaceEntity?

    @Query("SELECT * FROM tourist_places WHERE countryCode = :countryCode")
    suspend fun getAll(countryCode: String): List<TouristPlaceEntity>

    @Query(
        """
        SELECT * FROM tourist_places
        WHERE countryCode = :countryCode AND (
            name LIKE '%' || :q || '%' COLLATE NOCASE
            OR city LIKE '%' || :q || '%' COLLATE NOCASE
            OR region LIKE '%' || :q || '%' COLLATE NOCASE
            OR category LIKE '%' || :q || '%' COLLATE NOCASE
            OR shortDescription LIKE '%' || :q || '%' COLLATE NOCASE
            OR description LIKE '%' || :q || '%' COLLATE NOCASE
        )
        ORDER BY
            CASE
                WHEN name LIKE :q || '%' COLLATE NOCASE THEN 0
                WHEN name LIKE '%' || :q || '%' COLLATE NOCASE THEN 1
                WHEN city LIKE '%' || :q || '%' COLLATE NOCASE THEN 2
                ELSE 3
            END,
            rating DESC,
            name ASC
        LIMIT 80
        """
    )
    fun search(q: String, countryCode: String): Flow<List<TouristPlaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(places: List<TouristPlaceEntity>)

    @Query("SELECT COUNT(*) FROM tourist_places")
    suspend fun count(): Int

    @Query("DELETE FROM tourist_places")
    suspend fun clearAll()
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY savedAt DESC")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT placeId FROM favorites")
    fun observeIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE placeId = :placeId")
    suspend fun delete(placeId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE placeId = :placeId)")
    suspend fun exists(placeId: String): Boolean
}

@Dao
interface RecentSearchDao {
    @Query("SELECT * FROM recent_searches ORDER BY searchedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 12): Flow<List<RecentSearchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RecentSearchEntity)

    @Query("DELETE FROM recent_searches WHERE query = :query")
    suspend fun delete(query: String)
}

@Dao
interface RecentlyViewedDao {
    @Query("SELECT * FROM recently_viewed ORDER BY viewedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 20): Flow<List<RecentlyViewedEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RecentlyViewedEntity)
}

@Dao
interface WeatherCacheDao {
    @Query("SELECT * FROM weather_cache WHERE cacheKey = :key LIMIT 1")
    suspend fun get(key: String): WeatherCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WeatherCacheEntity)
}
