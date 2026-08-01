package com.travelersmap.ui.features.place

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.WbCloudy
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travelersmap.domain.model.TouristPlace
import com.travelersmap.domain.model.WeatherInfo
import com.travelersmap.ui.components.EmptyState
import com.travelersmap.ui.components.GalleryThumb
import com.travelersmap.ui.components.HeroImageCarousel
import com.travelersmap.ui.components.LoadingSkeleton
import com.travelersmap.ui.components.PlaceMetaLine
import com.travelersmap.ui.components.RatingStars
import com.travelersmap.ui.components.SectionLabel
import com.travelersmap.ui.components.ShimmerBlock
import com.travelersmap.ui.theme.GlassCard
import com.travelersmap.ui.theme.TouristAccent

@Composable
fun PlaceDetailRoute(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onOpenNearby: (String) -> Unit,
    vm: PlaceDetailViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    when {
        state.loading -> LoadingSkeleton()
        state.place == null -> EmptyState("Place not found")
        else -> PlaceDetailScreen(
            place = state.place!!,
            nearby = state.nearby,
            isFavorite = state.isFavorite,
            weather = state.weather,
            weatherLoading = state.weatherLoading,
            weatherError = state.weatherError,
            onBack = onBack,
            onToggleFavorite = vm::toggleFavorite,
            onNavigate = { onNavigate(state.place!!.id) },
            onOpenNearby = onOpenNearby,
            onRetryWeather = vm::retryWeather
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailScreen(
    place: TouristPlace,
    nearby: List<TouristPlace>,
    isFavorite: Boolean,
    weather: WeatherInfo?,
    weatherLoading: Boolean,
    weatherError: String?,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onNavigate: () -> Unit,
    onOpenNearby: (String) -> Unit,
    onRetryWeather: () -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(place.name, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            if (isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) TouristAccent else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "${place.name} · ${place.city}, Uzbekistan\n${place.shortDescription}"
                            )
                        }
                        context.startActivity(Intent.createChooser(send, "Share place"))
                    }) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            HeroImageCarousel(
                urls = place.photoUrls,
                placeName = place.name,
                height = 260.dp
            )

            Column(Modifier.padding(horizontal = 20.dp)) {
                Text(
                    place.name,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = MaterialTheme.typography.headlineMedium.fontSize
                    )
                )
                Spacer(Modifier.height(4.dp))
                PlaceMetaLine(place)
                Spacer(Modifier.height(6.dp))
                RatingStars(place.rating)
                Spacer(Modifier.height(12.dp))
                Text(place.shortDescription, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilledTonalButton(onClick = onNavigate, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Outlined.Navigation, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Navigate")
                    }
                    FilledTonalButton(onClick = onToggleFavorite, modifier = Modifier.weight(1f)) {
                        Icon(
                            if (isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                            null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (isFavorite) "Saved" else "Save")
                    }
                }

                Spacer(Modifier.height(20.dp))
                InfoGrid(place)

                val contactBits = listOfNotNull(
                    place.website?.let { "Web · $it" },
                    place.phone?.let { "Phone · $it" }
                )
                if (contactBits.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    contactBits.forEach { line ->
                        Text(
                            line,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                SectionLabel("History")
                Text(place.history, style = MaterialTheme.typography.bodyMedium)

                Spacer(Modifier.height(20.dp))
                SectionLabel("About")
                Text(place.description, style = MaterialTheme.typography.bodyMedium)

                if (place.photoUrls.size > 1) {
                    Spacer(Modifier.height(20.dp))
                    SectionLabel("Gallery")
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        place.photoUrls.forEach { u ->
                            GalleryThumb(url = u)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                SectionLabel("Weather")
                WeatherCard(
                    weather = weather,
                    loading = weatherLoading,
                    error = weatherError,
                    onRetry = onRetryWeather
                )

                if (nearby.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    SectionLabel("Nearby attractions")
                    nearby.forEach { n ->
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .clickable { onOpenNearby(n.id) },
                            corner = 16.dp
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Text(n.name, style = MaterialTheme.typography.titleLarge)
                                PlaceMetaLine(n)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun WeatherCard(
    weather: WeatherInfo?,
    loading: Boolean,
    error: String?,
    onRetry: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth(), corner = 16.dp) {
        when {
            loading && weather == null -> {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ShimmerBlock(Modifier.fillMaxWidth().height(28.dp))
                    ShimmerBlock(Modifier.fillMaxWidth(0.7f).height(16.dp))
                    ShimmerBlock(Modifier.fillMaxWidth().height(48.dp))
                }
            }
            weather != null -> {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.WbCloudy, null, tint = TouristAccent)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "${weather.temperatureC.toInt()}°C · ${weather.condition}",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                weather.todaySummary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        WeatherChip(
                            Icons.Outlined.Thermostat,
                            "Feels ${weather.feelsLikeC.toInt()}°"
                        )
                        WeatherChip(
                            Icons.Outlined.WaterDrop,
                            "${weather.humidityPercent}%"
                        )
                        WeatherChip(
                            Icons.Outlined.Air,
                            "${weather.windSpeedKmh.toInt()} km/h"
                        )
                    }
                    Text(
                        "High ${weather.todayHighC.toInt()}° · Low ${weather.todayLowC.toInt()}°",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        error ?: "Weather unavailable",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = onRetry) { Text("Retry") }
                }
            }
        }
    }
}

@Composable
private fun WeatherChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    GlassCard(modifier = Modifier, corner = 12.dp) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, modifier = Modifier.height(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun InfoGrid(place: TouristPlace) {
    val items = listOf(
        "Opening hours" to place.openingHours,
        "Entry fee" to place.ticketPrice,
        "Visit time" to "${place.estimatedVisitMinutes} min",
        "Best season" to place.bestSeason,
        "Difficulty" to place.difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
        "Family" to if (place.familyFriendly) "Yes" else "No"
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (label, value) ->
                    GlassCard(modifier = Modifier.weight(1f), corner = 14.dp) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                label,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(value, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
