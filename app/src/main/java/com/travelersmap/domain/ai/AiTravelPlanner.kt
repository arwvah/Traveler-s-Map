package com.travelersmap.domain.ai

import com.travelersmap.domain.model.AiItinerary
import com.travelersmap.domain.model.TouristPlace
import kotlinx.coroutines.flow.Flow

/**
 * Swappable AI contract. Production uses Groq Llama; tests/mocks inject alternatives.
 */
interface AiTravelPlanner {
    suspend fun plan(prompt: String, catalog: List<TouristPlace>): AiItinerary

    /**
     * Streaming chat tokens. [messages] are role/content pairs ("user"/"assistant").
     */
    fun streamChat(
        messages: List<Pair<String, String>>,
        catalogHint: String = ""
    ): Flow<String> = kotlinx.coroutines.flow.flow {
        val plan = plan(
            messages.lastOrNull { it.first == "user" }?.second.orEmpty(),
            emptyList()
        )
        emit(plan.summary.ifBlank { plan.title })
    }
}
