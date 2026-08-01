package com.travelersmap.data.seed

import android.content.Context
import com.travelersmap.domain.model.Difficulty
import com.travelersmap.domain.model.PlaceCategory
import com.travelersmap.domain.model.TouristPlace
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader

object AttractionsJsonLoader {

    private const val ASSET = "uzbekistan_attractions.json"

    fun catalogVersion(context: Context): Int {
        val text = context.assets.open(ASSET).bufferedReader().use(BufferedReader::readText)
        return JSONObject(text).optInt("version", 0)
    }

    fun loadFromAssets(context: Context): List<TouristPlace> {
        val text = context.assets.open(ASSET).bufferedReader().use(BufferedReader::readText)
        val root = JSONObject(text)
        val arr = root.getJSONArray("places")
        return (0 until arr.length()).map { i -> arr.getJSONObject(i).toPlace() }
    }

    private fun JSONObject.toPlace(): TouristPlace {
        val photos = optJSONArray("photoUrls").toStringList()
        val nearby = optJSONArray("nearbyIds").toStringList()
        val category = optString("category", "HISTORICAL")
        val difficulty = optString("difficulty", "EASY")
        return TouristPlace(
            id = getString("id"),
            countryCode = optString("countryCode", "UZ"),
            name = getString("name"),
            city = getString("city"),
            region = optString("region", getString("city")),
            category = runCatching { PlaceCategory.valueOf(category) }
                .getOrDefault(PlaceCategory.HISTORICAL),
            shortDescription = optString("shortDescription"),
            description = optString("description"),
            history = optString("history"),
            latitude = getDouble("latitude"),
            longitude = getDouble("longitude"),
            photoUrls = photos,
            openingHours = stringOrFallback("openingHours", "Daily 09:00-18:00"),
            ticketPrice = stringOrFallback("ticketPrice", "Varies"),
            estimatedVisitMinutes = optInt("estimatedVisitMinutes", 60),
            bestSeason = stringOrFallback("bestSeason", "Apr-Jun, Sep-Oct"),
            difficulty = runCatching { Difficulty.valueOf(difficulty) }
                .getOrDefault(Difficulty.EASY),
            familyFriendly = optBoolean("familyFriendly", true),
            rating = optDouble("rating", 4.5).toFloat(),
            nearbyIds = nearby,
            website = optString("website").takeIf { it.isNotBlank() && it != "null" },
            phone = optString("phone").takeIf { it.isNotBlank() && it != "null" },
            accentColorHex = optLong("accentColorHex", 0xFFC9A227)
        )
    }

    /** Handles legacy bad generator output where hours were written as integers. */
    private fun JSONObject.stringOrFallback(key: String, fallback: String): String {
        if (!has(key) || isNull(key)) return fallback
        return when (val v = get(key)) {
            is String -> v.ifBlank { fallback }
            is Number -> fallback
            else -> v.toString().ifBlank { fallback }
        }
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return (0 until length()).mapNotNull { i ->
            optString(i).takeIf { it.isNotBlank() && it != "null" }
        }
    }
}
