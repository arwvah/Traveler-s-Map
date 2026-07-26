package com.travelersmap.ui.features.ai

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.travelersmap.domain.ai.AiTravelPlanner
import com.travelersmap.domain.model.AiItinerary
import com.travelersmap.domain.model.TouristPlace
import com.travelersmap.domain.repository.PlaceRepository
import com.travelersmap.ui.theme.GlassCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiUiState(
    val prompt: String = "",
    val loading: Boolean = false,
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

    init {
        viewModelScope.launch {
            val all = places.getAll()
            _state.update { it.copy(placeLookup = all.associateBy { p -> p.id }) }
        }
    }

    fun onPrompt(value: String) = _state.update { it.copy(prompt = value) }

    fun useSuggestion(text: String) {
        _state.update { it.copy(prompt = text) }
        generate()
    }

    fun generate() {
        val prompt = _state.value.prompt.trim()
        if (prompt.isEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching {
                val catalog = places.getAll()
                ai.plan(prompt, catalog)
            }.onSuccess { plan ->
                _state.update { it.copy(loading = false, itinerary = plan) }
            }.onFailure { e ->
                _state.update { it.copy(loading = false, error = e.message ?: "Failed") }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiPlannerScreen(
    onOpenPlace: (String) -> Unit,
    vm: AiPlannerViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val suggestions = listOf(
        "I have 2 days in Samarkand.",
        "I want nature.",
        "I love history.",
        "I'm traveling with family."
    )

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("AI Travel Planner", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Mock intelligence for MVP — architecture ready for a real API.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.prompt,
            onValueChange = vm::onPrompt,
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(16.dp),
            placeholder = { Text("Describe your trip…") }
        )
        Spacer(Modifier.height(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            suggestions.forEach { s ->
                AssistChip(onClick = { vm.useSuggestion(s) }, label = { Text(s) })
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = vm::generate,
            enabled = !state.loading && state.prompt.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.loading) "Planning…" else "Generate itinerary")
        }

        if (state.loading) {
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator(Modifier.padding(8.dp))
        }

        state.error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        state.itinerary?.let { plan ->
            Spacer(Modifier.height(20.dp))
            Text(plan.title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Text(plan.summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            plan.days.forEach { day ->
                GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), corner = 18.dp) {
                    Column(Modifier.padding(16.dp)) {
                        Text(day.title, style = MaterialTheme.typography.titleLarge)
                        Text(day.notes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
}
