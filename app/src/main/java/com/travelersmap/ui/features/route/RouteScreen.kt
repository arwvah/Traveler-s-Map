package com.travelersmap.ui.features.route

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.travelersmap.domain.model.RouteEstimate
import com.travelersmap.domain.model.TouristPlace
import com.travelersmap.domain.model.TravelMode
import com.travelersmap.domain.repository.PlaceRepository
import com.travelersmap.domain.repository.RouteRepository
import com.travelersmap.ui.theme.GlassCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RouteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    places: PlaceRepository,
    private val routes: RouteRepository
) : ViewModel() {
    private val placeId: String = checkNotNull(savedStateHandle["placeId"])

    val place: StateFlow<TouristPlace?> = places.observePlace(placeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun estimate(place: TouristPlace, mode: TravelMode): RouteEstimate {
        // MVP origin: Tashkent center as default user location
        return routes.estimate(41.2995, 69.2401, place.latitude, place.longitude, mode)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteScreen(
    onBack: () -> Unit,
    vm: RouteViewModel = hiltViewModel()
) {
    val place by vm.place.collectAsStateWithLifecycle()
    var mode by remember { mutableStateOf(TravelMode.DRIVING) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Route") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            val p = place
            if (p == null) {
                Text("Loading…")
            } else {
                Text("To ${p.name}", style = MaterialTheme.typography.headlineMedium)
                Text("From Tashkent (default origin · MVP)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TravelMode.entries.forEach { m ->
                        FilterChip(
                            selected = mode == m,
                            onClick = { mode = m },
                            label = {
                                Text(
                                    when (m) {
                                        TravelMode.WALKING -> "Walk"
                                        TravelMode.DRIVING -> "Drive"
                                        TravelMode.CYCLING -> "Cycle"
                                        TravelMode.TRANSIT -> "Transit"
                                    }
                                )
                            }
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                val est = vm.estimate(p, mode)
                GlassCard(modifier = Modifier.fillMaxWidth(), corner = 20.dp) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Distance", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("%.1f km".format(est.distanceKm), style = MaterialTheme.typography.headlineMedium)
                        Text("Estimated time", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatMinutes(est.durationMinutes), style = MaterialTheme.typography.headlineMedium)
                        if (est.isPlaceholder) {
                            Text(
                                "Public transport is a placeholder for MVP. Hook Directions API later.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                "Offline haversine estimate · not turn-by-turn.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatMinutes(m: Int): String {
    val h = m / 60
    val min = m % 60
    return if (h > 0) "${h}h ${min}m" else "${min} min"
}
