package com.travelersmap.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TouristPlaceEntity::class, FavoriteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao
    abstract fun favoriteDao(): FavoriteDao
}
