package com.travelersmap.ui.features.ai

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.travelersmap.domain.ai.AiTravelPlanner
import com.travelersmap.domain.model.AiChatMessage
import com.travelersmap.domain.model.AiItinerary
import com.travelersmap.domain.model.AiMessageRole
import com.travelersmap.domain.model.TouristPlace
import com.travelersmap.domain.repository.PlaceRepository
import com.travelersmap.ui.theme.GlassCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AiUiState(
    val prompt: String = "",
    val loading: Boolean = false,
    val streamingText: String = "",
    val messages: List<AiChatMessage> = emptyList(),
    val itinerary: AiItinerary? = null,
    val placeLookup: Map<String, TouristPlace> = emptyMap(),
    val error: String? = null
)

@HiltViewModel
class AiPlannerViewModel @Inject constructor(
    private val ai: AiTravelPlanner,
    private val places: PlaceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AiUiState())
    val state: StateFlow<AiUiState> = _state.asStateFlow()

    private var catalogHint: String = ""

    init {
        viewModelScope.launch {
            val all = places.getAll()
            catalogHint = all
                .sortedByDescending { it.rating }
                .take(50)
                .joinToString("\n") { "- ${it.name} (${it.city})" }
            _state.update { it.copy(placeLookup = all.associateBy { p -> p.id }) }
        }
    }

    fun onPrompt(value: String) = _state.update { it.copy(prompt = value) }

    fun useSuggestion(text: String) {
        _state.update { it.copy(prompt = text) }
        send()
    }

    fun send() {
        val prompt = _state.value.prompt.trim()
        if (prompt.isEmpty() || _state.value.loading) return

        val userMsg = AiChatMessage(
            id = UUID.randomUUID().toString(),
            role = AiMessageRole.USER,
            content = prompt
        )
        _state.update {
            it.copy(
                loading = true,
                error = null,
                prompt = "",
                streamingText = "",
                messages = it.messages + userMsg,
                itinerary = null
            )
        }

        viewModelScope.launch {
            // Structured itinerary for trip-planning prompts
            val wantsPlan = prompt.contains("day", ignoreCase = true) ||
                prompt.contains("trip", ignoreCase = true) ||
                prompt.contains("plan", ignoreCase = true) ||
                prompt.contains("weekend", ignoreCase = true)

            if (wantsPlan) {
                runCatching {
                    ai.plan(prompt, places.getAll())
                }.onSuccess { plan ->
                    val reply = buildString {
                        appendLine(plan.title)
                        appendLine()
                        appendLine(plan.summary)
                        plan.days.forEach { day ->
                            appendLine()
                            appendLine(day.title)
                            appendLine(day.notes)
                        }
                    }
                    val assistant = AiChatMessage(
                        id = UUID.randomUUID().toString(),
                        role = AiMessageRole.ASSISTANT,
                        content = reply.trim()
                    )
                    _state.update {
                        it.copy(
                            loading = false,
                            itinerary = plan,
                            messages = it.messages + assistant,
                            streamingText = ""
                        )
                    }
                }.onFailure { e ->
                    _state.update {
                        it.copy(loading = false, error = e.message ?: "AI failed")
                    }
                }
                return@launch
            }

            // Streaming chat for general questions
            val history = _state.value.messages
                .filter { it.role != AiMessageRole.SYSTEM }
                .map { msg ->
                    val role = when (msg.role) {
                        AiMessageRole.USER -> "user"
                        AiMessageRole.ASSISTANT -> "assistant"
                        AiMessageRole.SYSTEM -> "system"
                    }
                    role to msg.content
                }
            val buffer = StringBuilder()
            runCatching {
                ai.streamChat(history, catalogHint).collect { token ->
                    buffer.append(token)
                    _state.update { it.copy(streamingText = buffer.toString()) }
                }
            }.onSuccess {
                val assistant = AiChatMessage(
                    id = UUID.randomUUID().toString(),
                    role = AiMessageRole.ASSISTANT,
                    content = buffer.toString().ifBlank { "No response." }
                )
                _state.update {
                    it.copy(
                        loading = false,
                        messages = it.messages + assistant,
                        streamingText = ""
                    )
                }
            }.onFailure { e ->
                _state.update {
                    it.copy(loading = false, error = e.message ?: "Stream failed")
                }
            }
        }
    }

    /** Kept for compatibility with older UI hooks. */
    fun generate() = send()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiPlannerScreen(
    onOpenPlace: (String) -> Unit,
    vm: AiPlannerViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val suggestions = listOf(
        "Plan a 2-day trip to Samarkand.",
        "Hidden places in Bukhara.",
        "Best nature destinations.",
        "Weekend trip from Tashkent."
    )

    LaunchedEffect(state.messages.size, state.streamingText) {
        val last = state.messages.size + if (state.streamingText.isNotEmpty()) 1 else 0
        if (last > 0) listState.animateScrollToItem(last - 1)
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("AI Travel Planner", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Powered by Groq Llama · Uzbekistan travel only",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            suggestions.forEach { s ->
                AssistChip(onClick = { vm.useSuggestion(s) }, label = { Text(s) })
            }
        }
        Spacer(Modifier.height(12.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.messages, key = { it.id }) { msg ->
                val isUser = msg.role == AiMessageRole.USER
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(0.92f),
                        corner = 16.dp
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                if (isUser) "You" else "Assistant",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(msg.content, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
            if (state.streamingText.isNotEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth(0.92f), corner = 16.dp) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                "Assistant",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(state.streamingText, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
            if (state.loading && state.streamingText.isEmpty() && state.itinerary == null) {
                item {
                    Row(
                        Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(Modifier.height(24.dp))
                        Spacer(Modifier.padding(8.dp))
                        Text("Thinking…", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            state.itinerary?.let { plan ->
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(plan.title, style = MaterialTheme.typography.titleLarge)
                    Text(
                        plan.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(plan.days) { day ->
                    GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp), corner = 18.dp) {
                        Column(Modifier.padding(16.dp)) {
                            Text(day.title, style = MaterialTheme.typography.titleLarge)
                            Text(
                                day.notes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            day.placeIds.forEach { id ->
                                val p = state.placeLookup[id]
                                Text(
                                    text = "· ${p?.name ?: id}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onOpenPlace(id) }
                                        .padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 4.dp))
        }

        OutlinedTextField(
            value = state.prompt,
            onValueChange = vm::onPrompt,
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4,
            shape = RoundedCornerShape(16.dp),
            placeholder = { Text("Ask about Uzbekistan travel…") }
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = vm::send,
            enabled = !state.loading && state.prompt.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.loading) "Planning…" else "Send")
        }
    }
}
