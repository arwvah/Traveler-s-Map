package com.travelersmap.di

import android.content.Context
import androidx.room.Room
import com.travelersmap.data.ai.MockAiTravelPlanner
import com.travelersmap.data.local.AppDatabase
import com.travelersmap.data.local.FavoriteDao
import com.travelersmap.data.local.PlaceDao
import com.travelersmap.data.local.toEntity
import com.travelersmap.data.repository.BudgetRepositoryImpl
import com.travelersmap.data.repository.FavoriteRepositoryImpl
import com.travelersmap.data.repository.PlaceRepositoryImpl
import com.travelersmap.data.repository.RouteRepositoryImpl
import com.travelersmap.data.repository.SettingsRepositoryImpl
import com.travelersmap.data.seed.UzbekistanSeedData
import com.travelersmap.domain.ai.AiTravelPlanner
import com.travelersmap.domain.repository.BudgetRepository
import com.travelersmap.domain.repository.FavoriteRepository
import com.travelersmap.domain.repository.PlaceRepository
import com.travelersmap.domain.repository.RouteRepository
import com.travelersmap.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        val db = Room.databaseBuilder(context, AppDatabase::class.java, "travelers_map.db")
            .fallbackToDestructiveMigration()
            .build()

        // Seed offline Uzbekistan catalog before first UI observe (fast, local-only).
        runBlocking(Dispatchers.IO) {
            if (db.placeDao().count() == 0) {
                db.placeDao().upsertAll(UzbekistanSeedData.places.map { it.toEntity() })
            }
        }
        return db
    }

    @Provides fun placeDao(db: AppDatabase): PlaceDao = db.placeDao()
    @Provides fun favoriteDao(db: AppDatabase): FavoriteDao = db.favoriteDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun places(impl: PlaceRepositoryImpl): PlaceRepository
    @Binds @Singleton abstract fun favorites(impl: FavoriteRepositoryImpl): FavoriteRepository
    @Binds @Singleton abstract fun routes(impl: RouteRepositoryImpl): RouteRepository
    @Binds @Singleton abstract fun budget(impl: BudgetRepositoryImpl): BudgetRepository
    @Binds @Singleton abstract fun settings(impl: SettingsRepositoryImpl): SettingsRepository
    @Binds @Singleton abstract fun ai(impl: MockAiTravelPlanner): AiTravelPlanner
}
