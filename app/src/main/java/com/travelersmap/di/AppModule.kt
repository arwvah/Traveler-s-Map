package com.travelersmap.di

import android.content.Context
import androidx.room.Room
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import com.travelersmap.BuildConfig
import com.travelersmap.data.ai.GroqAiTravelPlanner
import com.travelersmap.data.local.AppDatabase
import com.travelersmap.data.local.FavoriteDao
import com.travelersmap.data.local.PlaceDao
import com.travelersmap.data.local.RecentSearchDao
import com.travelersmap.data.local.RecentlyViewedDao
import com.travelersmap.data.local.WeatherCacheDao
import com.travelersmap.data.local.toEntity
import com.travelersmap.data.remote.NetworkClients
import com.travelersmap.data.repository.BudgetRepositoryImpl
import com.travelersmap.data.repository.FavoriteRepositoryImpl
import com.travelersmap.data.repository.PlaceRepositoryImpl
import com.travelersmap.data.repository.RouteRepositoryImpl
import com.travelersmap.data.repository.SettingsRepositoryImpl
import com.travelersmap.data.repository.WeatherRepositoryImpl
import com.travelersmap.data.seed.AttractionsJsonLoader
import com.travelersmap.data.seed.UzbekistanSeedData
import com.travelersmap.domain.ai.AiTravelPlanner
import com.travelersmap.domain.repository.BudgetRepository
import com.travelersmap.domain.repository.FavoriteRepository
import com.travelersmap.domain.repository.PlaceRepository
import com.travelersmap.domain.repository.RouteRepository
import com.travelersmap.domain.repository.SettingsRepository
import com.travelersmap.domain.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val MIN_CATALOG_SIZE = 200
    private const val CATALOG_PREFS = "travelers_map_catalog"
    private const val KEY_CATALOG_VERSION = "catalog_version"

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        val db = Room.databaseBuilder(context, AppDatabase::class.java, "travelers_map.db")
            .fallbackToDestructiveMigration()
            .build()

        runBlocking(Dispatchers.IO) {
            val prefs = context.getSharedPreferences(CATALOG_PREFS, Context.MODE_PRIVATE)
            val storedVersion = prefs.getInt(KEY_CATALOG_VERSION, 0)
            val assetVersion = runCatching { AttractionsJsonLoader.catalogVersion(context) }
                .getOrDefault(0)
            val count = db.placeDao().count()
            // Reseed when empty/tiny, or when the JSON asset version advances (image/data fixes).
            val needsReseed = count < MIN_CATALOG_SIZE ||
                (assetVersion > 0 && assetVersion > storedVersion)
            if (needsReseed) {
                val fromJson = runCatching { AttractionsJsonLoader.loadFromAssets(context) }
                    .getOrDefault(emptyList())
                val catalog = if (fromJson.size >= MIN_CATALOG_SIZE) {
                    fromJson
                } else {
                    UzbekistanSeedData.places
                }
                db.placeDao().clearAll()
                db.placeDao().upsertAll(catalog.map { it.toEntity() })
                prefs.edit()
                    .putInt(KEY_CATALOG_VERSION, maxOf(assetVersion, 1))
                    .apply()
            }
        }
        return db
    }

    @Provides fun placeDao(db: AppDatabase): PlaceDao = db.placeDao()
    @Provides fun favoriteDao(db: AppDatabase): FavoriteDao = db.favoriteDao()
    @Provides fun recentSearchDao(db: AppDatabase): RecentSearchDao = db.recentSearchDao()
    @Provides fun recentlyViewedDao(db: AppDatabase): RecentlyViewedDao = db.recentlyViewedDao()
    @Provides fun weatherCacheDao(db: AppDatabase): WeatherCacheDao = db.weatherCacheDao()
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient = NetworkClients.defaultOkHttp()

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        okHttp: OkHttpClient
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .okHttpClient {
                okHttp.newBuilder()
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .build()
            }
            .crossfade(true)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(120L * 1024 * 1024)
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false)
            .apply {
                if (BuildConfig.DEBUG) logger(DebugLogger())
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideMapsApiKey(): String = BuildConfig.MAPS_API_KEY
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun places(impl: PlaceRepositoryImpl): PlaceRepository
    @Binds @Singleton abstract fun favorites(impl: FavoriteRepositoryImpl): FavoriteRepository
    @Binds @Singleton abstract fun routes(impl: RouteRepositoryImpl): RouteRepository
    @Binds @Singleton abstract fun budget(impl: BudgetRepositoryImpl): BudgetRepository
    @Binds @Singleton abstract fun settings(impl: SettingsRepositoryImpl): SettingsRepository
    @Binds @Singleton abstract fun weather(impl: WeatherRepositoryImpl): WeatherRepository
    @Binds @Singleton abstract fun ai(impl: GroqAiTravelPlanner): AiTravelPlanner
}
