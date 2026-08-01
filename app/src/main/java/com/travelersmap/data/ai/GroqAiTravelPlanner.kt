package com.travelersmap.data.ai

import com.travelersmap.BuildConfig
import com.travelersmap.domain.ai.AiTravelPlanner
import com.travelersmap.domain.model.AiItinerary
import com.travelersmap.domain.model.AiItineraryDay
import com.travelersmap.domain.model.TouristPlace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Groq-backed Llama planner. Provider-swappable via [AiTravelPlanner].
 */
@Singleton
class GroqAiTravelPlanner @Inject constructor(
    private val client: OkHttpClient,
    private val mock: MockAiTravelPlanner
) : AiTravelPlanner {

    private val model = "llama-3.3-70b-versatile"
    private val endpoint = "https://api.groq.com/openai/v1/chat/completions"

    private val systemPrompt = """
        You are Traveler's Map AI, an expert travel assistant ONLY for Uzbekistan tourism.
        Answer only about travel, attractions, culture, food, routes, seasons, and safety in Uzbekistan.
        If asked about other countries or unrelated topics, politely redirect to Uzbekistan travel.
        Prefer concrete place names, cities, and practical tips.
        Keep answers clear and useful for travelers.
        When planning multi-day trips, structure with Day 1, Day 2, etc.
    """.trimIndent()

    override suspend fun plan(prompt: String, catalog: List<TouristPlace>): AiItinerary {
        val key = BuildConfig.GROQ_API_KEY
        if (key.isBlank() || key == "YOUR_GROQ_API_KEY") {
            return mock.plan(prompt, catalog)
        }
        return withContext(Dispatchers.IO) {
            val sample = catalog
                .sortedByDescending { it.rating }
                .take(40)
                .joinToString("\n") { "- ${it.name} (${it.city}, ${it.category.name})" }
            val user = buildString {
                appendLine("User request: $prompt")
                appendLine()
                appendLine("Catalog sample (use these ids/names when relevant):")
                appendLine(sample)
                appendLine()
                appendLine(
                    "Respond as JSON only with keys: title, summary, days " +
                        "(array of {dayNumber, title, placeNames, notes})."
                )
            }
            val content = chatOnce(
                listOf(
                    "system" to systemPrompt,
                    "user" to user
                )
            )
            parseItinerary(content, catalog) ?: mock.plan(prompt, catalog).copy(
                summary = content.take(800)
            )
        }
    }

    override fun streamChat(
        messages: List<Pair<String, String>>,
        catalogHint: String
    ): Flow<String> = flow {
        val key = BuildConfig.GROQ_API_KEY
        if (key.isBlank() || key == "YOUR_GROQ_API_KEY") {
            emit(
                "Add GROQ_API_KEY to local.properties to enable live Llama answers. " +
                    "Meanwhile: explore Samarkand, Bukhara, Khiva, Chimgan, and Zaamin — " +
                    "Uzbekistan's classic travel circuit."
            )
            return@flow
        }
        val payload = JSONObject().apply {
            put("model", model)
            put("stream", true)
            put("temperature", 0.6)
            put(
                "messages",
                JSONArray().apply {
                    put(JSONObject().put("role", "system").put("content", systemPrompt + "\n\nCatalog hint:\n$catalogHint"))
                    messages.forEach { (role, content) ->
                        put(JSONObject().put("role", role).put("content", content))
                    }
                }
            )
        }
        val request = Request.Builder()
            .url(endpoint)
            .header("Authorization", "Bearer $key")
            .header("Content-Type", "application/json")
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                emit("AI error: HTTP ${response.code}. Check your Groq API key.")
                return@use
            }
            val source = response.body?.source() ?: return@use
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: break
                if (!line.startsWith("data:")) continue
                val data = line.removePrefix("data:").trim()
                if (data == "[DONE]") break
                runCatching {
                    val json = JSONObject(data)
                    val delta = json.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("delta")
                    val token = delta.optString("content")
                    if (token.isNotEmpty()) emit(token)
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun chatOnce(messages: List<Pair<String, String>>): String {
        val key = BuildConfig.GROQ_API_KEY
        val payload = JSONObject().apply {
            put("model", model)
            put("stream", false)
            put("temperature", 0.5)
            put(
                "messages",
                JSONArray().apply {
                    messages.forEach { (role, content) ->
                        put(JSONObject().put("role", role).put("content", content))
                    }
                }
            )
        }
        val request = Request.Builder()
            .url(endpoint)
            .header("Authorization", "Bearer $key")
            .header("Content-Type", "application/json")
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Groq HTTP ${response.code}")
            val json = JSONObject(response.body?.string().orEmpty())
            return json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        }
    }

    private fun parseItinerary(raw: String, catalog: List<TouristPlace>): AiItinerary? {
        val start = raw.indexOf('{')
        val end = raw.lastIndexOf('}')
        if (start < 0 || end <= start) return null
        return runCatching {
            val json = JSONObject(raw.substring(start, end + 1))
            val byName = catalog.associateBy { it.name.lowercase() }
            val daysArr = json.optJSONArray("days") ?: JSONArray()
            val days = (0 until daysArr.length()).map { i ->
                val d = daysArr.getJSONObject(i)
                val names = d.optJSONArray("placeNames")
                val ids = buildList {
                    if (names != null) {
                        for (j in 0 until names.length()) {
                            val n = names.optString(j).lowercase()
                            byName[n]?.id?.let { add(it) }
                                ?: catalog.find { it.name.lowercase().contains(n) }?.id?.let { add(it) }
                        }
                    }
                }
                AiItineraryDay(
                    dayNumber = d.optInt("dayNumber", i + 1),
                    title = d.optString("title", "Day ${i + 1}"),
                    placeIds = ids,
                    notes = d.optString("notes")
                )
            }
            AiItinerary(
                title = json.optString("title", "Uzbekistan itinerary"),
                summary = json.optString("summary"),
                days = days
            )
        }.getOrNull()
    }
}
