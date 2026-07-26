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
            OR category LIKE '%' || :q || '%' COLLATE NOCASE
            OR shortDescription LIKE '%' || :q || '%' COLLATE NOCASE
        )
        ORDER BY name ASC
        """
    )
    fun search(q: String, countryCode: String): Flow<List<TouristPlaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(places: List<TouristPlaceEntity>)

    @Query("SELECT COUNT(*) FROM tourist_places")
    suspend fun count(): Int
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
