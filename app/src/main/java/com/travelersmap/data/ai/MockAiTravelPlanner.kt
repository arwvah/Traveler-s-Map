package com.travelersmap.data.ai

import com.travelersmap.domain.ai.AiTravelPlanner
import com.travelersmap.domain.model.AiItinerary
import com.travelersmap.domain.model.AiItineraryDay
import com.travelersmap.domain.model.PlaceCategory
import com.travelersmap.domain.model.TouristPlace
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Deterministic mock planner. Replace with LLM API implementation of [AiTravelPlanner].
 */
@Singleton
class MockAiTravelPlanner @Inject constructor() : AiTravelPlanner {

    override suspend fun plan(prompt: String, catalog: List<TouristPlace>): AiItinerary {
        delay(600) // simulate network / inference latency
        val p = prompt.lowercase()
        val days = Regex("""(\d+)\s*day""").find(p)?.groupValues?.get(1)?.toIntOrNull() ?: 2

        val cityHint = listOf(
            "samarkand", "bukhara", "khiva", "tashkent", "shahrisabz",
            "nukus", "termez", "chimgan", "charvak", "zaamin", "nuratau", "aydarkul"
        ).firstOrNull { p.contains(it) }

        var pool = catalog
        if (cityHint != null) {
            pool = catalog.filter { it.city.lowercase().contains(cityHint) || it.name.lowercase().contains(cityHint) }
            if (pool.isEmpty()) pool = catalog
        }

        val natureCats = setOf(
            PlaceCategory.MOUNTAIN, PlaceCategory.LAKE, PlaceCategory.WATERFALL,
            PlaceCategory.NATIONAL_PARK, PlaceCategory.NATURE_RESERVE, PlaceCategory.VIEWPOINT
        )
        val historyCats = setOf(
            PlaceCategory.HISTORICAL, PlaceCategory.MONUMENT, PlaceCategory.MUSEUM,
            PlaceCategory.MOSQUE, PlaceCategory.MADRASAH, PlaceCategory.FORTRESS,
            PlaceCategory.PALACE, PlaceCategory.UNESCO, PlaceCategory.ANCIENT_CITY
        )

        pool = when {
            p.contains("nature") || p.contains("mountain") || p.contains("lake") ->
                pool.filter { it.category in natureCats }.ifEmpty { pool }
            p.contains("history") || p.contains("museum") || p.contains("unesco") ->
                pool.filter { it.category in historyCats }.ifEmpty { pool }
            p.contains("family") ->
                pool.filter { it.familyFriendly }.ifEmpty { pool }
            else -> pool
        }

        val sorted = pool.sortedByDescending { it.rating }
        val perDay = 3
        val itineraryDays = (1..days).map { day ->
            val slice = sorted.drop((day - 1) * perDay).take(perDay).ifEmpty {
                sorted.take(perDay)
            }
            AiItineraryDay(
                dayNumber = day,
                title = "Day $day · ${slice.firstOrNull()?.city ?: "Uzbekistan"}",
                placeIds = slice.map { it.id },
                notes = when {
                    p.contains("family") -> "Family-friendly pacing with breaks for meals and rest."
                    p.contains("nature") -> "Pack water, sun protection, and comfortable shoes."
                    else -> "Start early to enjoy softer light and fewer crowds."
                }
            )
        }

        val titleCity = cityHint?.replaceFirstChar { it.uppercase() } ?: "Uzbekistan"
        return AiItinerary(
            title = "$days-day plan · $titleCity",
            summary = "Mock AI itinerary based on: \"$prompt\". Swap MockAiTravelPlanner for a live model later.",
            days = itineraryDays
        )
    }
}
