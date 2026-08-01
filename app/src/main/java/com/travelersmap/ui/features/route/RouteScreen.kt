package com.travelersmap.ui.features.route

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.travelersmap.data.location.LocationTracker
import com.travelersmap.domain.model.RouteEstimate
import com.travelersmap.domain.model.TouristPlace
import com.travelersmap.domain.model.TravelMode
import com.travelersmap.domain.model.UserLocation
import com.travelersmap.domain.repository.PlaceRepository
import com.travelersmap.domain.repository.RouteRepository
import com.travelersmap.ui.theme.GlassCard
import com.travelersmap.ui.theme.TouristAccent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RouteUiState(
    val place: TouristPlace? = null,
    val mode: TravelMode = TravelMode.DRIVING,
    val origin: UserLocation? = null,
    val estimate: RouteEstimate? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val permissionNeeded: Boolean = false
)

@HiltViewModel
class RouteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    places: PlaceRepository,
    private val routes: RouteRepository,
    private val locationTracker: LocationTracker
) : ViewModel() {
    private val placeId: String = checkNotNull(savedStateHandle["placeId"])

    private val _state = MutableStateFlow(RouteUiState(permissionNeeded = !locationTracker.hasPermission()))
    val state: StateFlow<RouteUiState> = _state.asStateFlow()

    private val placeFlow = places.observePlace(placeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            placeFlow.collect { place ->
                _state.update { it.copy(place = place) }
                if (place != null) refreshRoute()
            }
        }
        if (locationTracker.hasPermission()) {
            viewModelScope.launch {
                locationTracker.observeLocation().collect { loc ->
                    if (loc != null) {
                        _state.update { it.copy(origin = loc, permissionNeeded = false) }
                        refreshRoute()
                    }
                }
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _state.update { it.copy(permissionNeeded = !granted) }
        if (granted) {
            viewModelScope.launch {
                val loc = locationTracker.currentLocation()
                if (loc != null) {
                    _state.update { it.copy(origin = loc) }
                    refreshRoute()
                }
                locationTracker.observeLocation().collect { live ->
                    if (live != null) {
                        _state.update { it.copy(origin = live) }
                    }
                }
            }
        } else {
            // Fallback origin: Tashkent center so route still works without GPS.
            _state.update {
                it.copy(origin = UserLocation(41.2995, 69.2401))
            }
            refreshRoute()
        }
    }

    fun setMode(mode: TravelMode) {
        _state.update { it.copy(mode = mode) }
        refreshRoute()
    }

    fun refreshRoute() {
        val place = _state.value.place ?: return
        val origin = _state.value.origin ?: UserLocation(41.2995, 69.2401).also { fallback ->
            _state.update { it.copy(origin = fallback) }
        }
        val mode = _state.value.mode
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching {
                routes.route(
                    origin.latitude,
                    origin.longitude,
                    place.latitude,
                    place.longitude,
                    mode
                )
            }.onSuccess { est ->
                _state.update { it.copy(loading = false, estimate = est) }
            }.onFailure { e ->
                val fallback = routes.estimate(
                    origin.latitude,
                    origin.longitude,
                    place.latitude,
                    place.longitude,
                    mode
                )
                _state.update {
                    it.copy(
                        loading = false,
                        estimate = fallback,
                        error = e.message
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteScreen(
    onBack: () -> Unit,
    vm: RouteViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        vm.onPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        if (state.permissionNeeded) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else if (state.origin == null) {
            vm.onPermissionResult(true)
        }
    }

    val place = state.place
    val origin = state.origin
    val camera = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(place?.latitude ?: 41.3, place?.longitude ?: 69.2),
            10f
        )
    }

    LaunchedEffect(state.estimate, origin, place) {
        val est = state.estimate
        val p = place
        val o = origin
        if (est != null && est.polyline.size >= 2) {
            runCatching {
                val b = LatLngBounds.builder()
                est.polyline.forEach { b.include(LatLng(it.latitude, it.longitude)) }
                camera.animate(CameraUpdateFactory.newLatLngBounds(b.build(), 100))
            }
        } else if (p != null && o != null) {
            runCatching {
                val b = LatLngBounds.builder()
                    .include(LatLng(o.latitude, o.longitude))
                    .include(LatLng(p.latitude, p.longitude))
                    .build()
                camera.animate(CameraUpdateFactory.newLatLngBounds(b, 120))
            }
        }
    }

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
        ) {
            if (place == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = camera,
                    uiSettings = remember {
                        MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false)
                    }
                ) {
                    if (origin != null) {
                        Marker(
                            state = MarkerState(LatLng(origin.latitude, origin.longitude)),
                            title = "You"
                        )
                    }
                    Marker(
                        state = MarkerState(LatLng(place.latitude, place.longitude)),
                        title = place.name
                    )
                    val poly = state.estimate?.polyline.orEmpty()
                    if (poly.size >= 2) {
                        Polyline(
                            points = poly.map { LatLng(it.latitude, it.longitude) },
                            color = TouristAccent,
                            width = 10f
                        )
                    }
                }
                if (state.loading) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }

            Column(Modifier.padding(16.dp)) {
                Text("To ${place.name}", style = MaterialTheme.typography.headlineMedium)
                val fromLabel = if (state.permissionNeeded || origin == null) {
                    "From Tashkent (default · enable location for live GPS)"
                } else {
                    "From your live GPS location"
                }
                Text(
                    fromLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TravelMode.entries.forEach { m ->
                        FilterChip(
                            selected = state.mode == m,
                            onClick = { vm.setMode(m) },
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

                Spacer(Modifier.height(16.dp))
                val est = state.estimate
                GlassCard(modifier = Modifier.fillMaxWidth(), corner = 20.dp) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (est == null) {
                            Text("Calculating route…", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            Text(
                                "Distance",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text("%.1f km".format(est.distanceKm), style = MaterialTheme.typography.headlineMedium)
                            Text(
                                "Estimated time",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(formatMinutes(est.durationMinutes), style = MaterialTheme.typography.headlineMedium)
                            Text(
                                "Mode · ${est.mode.name.lowercase().replaceFirstChar { it.uppercase() }}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (est.isPlaceholder) {
                                Text(
                                    "Transit estimate is approximate. Start Navigation uses Google Maps.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        state.error?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        val o = origin ?: UserLocation(41.2995, 69.2401)
                        val mode = when (state.mode) {
                            TravelMode.WALKING -> "w"
                            TravelMode.DRIVING -> "d"
                            TravelMode.CYCLING -> "b"
                            TravelMode.TRANSIT -> "r"
                        }
                        val uri = Uri.parse(
                            "google.navigation:q=${place.latitude},${place.longitude}&mode=$mode"
                        )
                        val maps = Intent(Intent.ACTION_VIEW, uri).apply {
                            setPackage("com.google.android.apps.maps")
                        }
                        runCatching { context.startActivity(maps) }
                            .recoverCatching {
                                val web = Uri.parse(
                                    "https://www.google.com/maps/dir/?api=1" +
                                        "&origin=${o.latitude},${o.longitude}" +
                                        "&destination=${place.latitude},${place.longitude}" +
                                        "&travelmode=${
                                            when (state.mode) {
                                                TravelMode.WALKING -> "walking"
                                                TravelMode.DRIVING -> "driving"
                                                TravelMode.CYCLING -> "bicycling"
                                                TravelMode.TRANSIT -> "transit"
                                            }
                                        }"
                                )
                                context.startActivity(Intent(Intent.ACTION_VIEW, web))
                            }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true
                ) {
                    Icon(Icons.Outlined.Navigation, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Start Navigation")
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
