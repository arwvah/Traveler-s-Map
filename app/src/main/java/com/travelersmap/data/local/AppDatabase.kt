package com.travelersmap.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        TouristPlaceEntity::class,
        FavoriteEntity::class,
        RecentSearchEntity::class,
        RecentlyViewedEntity::class,
        WeatherCacheEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun recentSearchDao(): RecentSearchDao
    abstract fun recentlyViewedDao(): RecentlyViewedDao
    abstract fun weatherCacheDao(): WeatherCacheDao
}
