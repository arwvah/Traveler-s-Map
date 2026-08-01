package com.travelersmap.ui.features.map

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import com.travelersmap.ui.components.CachedPlaceImage
import com.travelersmap.ui.components.LoadingSkeleton
import com.travelersmap.ui.components.PlaceMetaLine
import com.travelersmap.ui.components.RatingStars
import com.travelersmap.ui.theme.GlassCard
import com.travelersmap.ui.theme.TouristAccent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapHomeScreen(
    onOpenPlace: (String) -> Unit,
    onSearchSelect: (String) -> Unit,
    vm: MapViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val center = LatLng(state.country.centerLat, state.country.centerLng)
    val camera = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center, state.country.defaultZoom)
    }
    val scope = rememberCoroutineScope()
    var didInitialCenter by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        vm.onPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        if (!state.locationPermissionGranted) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Center near user once when first fix arrives.
    LaunchedEffect(state.userLocation, state.locationPermissionGranted) {
        val loc = state.userLocation
        if (!didInitialCenter && state.locationPermissionGranted && loc != null) {
            didInitialCenter = true
            camera.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(loc.latitude, loc.longitude),
                    11f
                )
            )
        }
    }

    // Recenter when My Location FAB is tapped.
    LaunchedEffect(state.centerOnUserToken) {
        if (state.centerOnUserToken == 0) return@LaunchedEffect
        val loc = state.userLocation
        if (loc != null) {
            camera.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(loc.latitude, loc.longitude),
                    14f
                )
            )
        }
    }

    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = true,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false
        )
    }
    val mapProperties = remember(state.locationPermissionGranted) {
        MapProperties(
            mapType = MapType.NORMAL,
            isBuildingEnabled = true,
            isMyLocationEnabled = state.locationPermissionGranted
        )
    }

    val clusterItems = remember(state.allPlaces) {
        state.allPlaces.map { PlaceClusterItem(it) }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val selectedPlace = state.allPlaces.find { it.id == state.selectedId }

    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = camera,
            properties = mapProperties,
            uiSettings = uiSettings,
            onMapClick = { vm.select(null) }
        ) {
            Clustering(
                items = clusterItems,
                onClusterClick = { cluster ->
                    scope.launch {
                        val builder = LatLngBounds.builder()
                        cluster.items.forEach { builder.include(it.position) }
                        runCatching {
                            camera.animate(
                                CameraUpdateFactory.newLatLngBounds(builder.build(), 120)
                            )
                        }
                    }
                    true
                },
                onClusterItemClick = { item ->
                    vm.select(item.place.id)
                    scope.launch {
                        camera.animate(
                            CameraUpdateFactory.newLatLngZoom(item.position, 12f)
                        )
                    }
                    true
                },
                clusterContent = { cluster ->
                    ClusterBubble(count = cluster.size)
                },
                clusterItemContent = { item ->
                    TouristPin(selected = item.place.id == state.selectedId)
                }
            )
        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter)
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth(), corner = 28.dp) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = vm::onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search cities, mosques, nature…") },
                    leadingIcon = { Icon(Icons.Outlined.Search, null) },
                    trailingIcon = {
                        if (state.query.isNotEmpty()) {
                            IconButton(onClick = { vm.onQueryChange("") }) {
                                Icon(Icons.Outlined.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
            }

            AnimatedVisibility(
                visible = state.query.isNotBlank(),
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                Spacer(Modifier.height(8.dp))
                GlassCard(modifier = Modifier.fillMaxWidth().height(280.dp), corner = 20.dp) {
                    if (state.isLoading) {
                        LoadingSkeleton()
                    } else if (state.places.isEmpty()) {
                        Text(
                            "No places match",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        LazyColumn(contentPadding = PaddingValues(8.dp)) {
                            items(state.places, key = { it.id }) { place ->
                                SearchRow(place.name, place.city) {
                                    vm.rememberSearch(state.query)
                                    onSearchSelect(place.id)
                                    vm.onQueryChange("")
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                if (!state.locationPermissionGranted) {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                } else {
                    vm.centerOnUser()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 72.dp),
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            contentColor = TouristAccent
        ) {
            Icon(Icons.Outlined.MyLocation, contentDescription = "My Location")
        }

        Text(
            text = "Uzbekistan · ${state.allPlaces.size} places",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }

    if (selectedPlace != null) {
        ModalBottomSheet(
            onDismissRequest = { vm.select(null) },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
            scrimColor = Color.Black.copy(alpha = 0.35f),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            PlacePreviewSheet(
                place = selectedPlace,
                onOpen = {
                    val id = selectedPlace.id
                    vm.select(null)
                    onOpenPlace(id)
                },
                onDismiss = { vm.select(null) }
            )
        }
    }

    LaunchedEffect(state.selectedId) {
        if (state.selectedId == null) {
            runCatching { sheetState.hide() }
        }
    }
}

@Composable
private fun PlacePreviewSheet(
    place: com.travelersmap.domain.model.TouristPlace,
    onOpen: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        val url = place.photoUrls.firstOrNull()
        if (url != null) {
            CachedPlaceImage(
                url = url,
                contentDescription = place.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
            Spacer(Modifier.height(14.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(TouristAccent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Place, null, tint = TouristAccent, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    place.name,
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                PlaceMetaLine(place)
                RatingStars(place.rating)
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Outlined.Close, contentDescription = "Close")
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            place.shortDescription,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "${place.openingHours} · ${place.ticketPrice}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(18.dp))
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpen),
            corner = 18.dp
        ) {
            Text(
                "Open full place page",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun SearchRow(title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
