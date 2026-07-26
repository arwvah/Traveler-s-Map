package com.travelersmap.domain.ai

import com.travelersmap.domain.model.AiItinerary
import com.travelersmap.domain.model.TouristPlace

/**
 * Swappable AI contract. MVP uses [MockAiTravelPlanner];
 * production can inject a real LLM-backed implementation without UI changes.
 */
interface AiTravelPlanner {
    suspend fun plan(prompt: String, catalog: List<TouristPlace>): AiItinerary
}
