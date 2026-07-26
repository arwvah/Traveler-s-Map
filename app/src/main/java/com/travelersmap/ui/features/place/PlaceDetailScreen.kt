package com.travelersmap.ui.features.place

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.WbCloudy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.travelersmap.domain.model.TouristPlace
import com.travelersmap.ui.components.EmptyState
import com.travelersmap.ui.components.LoadingSkeleton
import com.travelersmap.ui.components.PlaceMetaLine
import com.travelersmap.ui.components.RatingStars
import com.travelersmap.ui.components.SectionLabel
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
            onBack = onBack,
            onToggleFavorite = vm::toggleFavorite,
            onNavigate = { onNavigate(state.place!!.id) },
            onOpenNearby = onOpenNearby
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailScreen(
    place: TouristPlace,
    nearby: List<TouristPlace>,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onNavigate: () -> Unit,
    onOpenNearby: (String) -> Unit
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
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                val url = place.photoUrls.firstOrNull()
                if (url != null) {
                    AsyncImage(
                        model = url,
                        contentDescription = place.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(ColorTransparent, MaterialTheme.colorScheme.background)
                            )
                        )
                )
            }

            Column(Modifier.padding(horizontal = 20.dp)) {
                Text(place.name, style = MaterialTheme.typography.displayLarge.copy(fontSize = MaterialTheme.typography.headlineMedium.fontSize))
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
                        Icon(if (isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (isFavorite) "Saved" else "Save")
                    }
                }

                Spacer(Modifier.height(20.dp))
                InfoGrid(place)

                Spacer(Modifier.height(20.dp))
                SectionLabel("History")
                Text(place.history, style = MaterialTheme.typography.bodyMedium)

                Spacer(Modifier.height(20.dp))
                SectionLabel("About")
                Text(place.description, style = MaterialTheme.typography.bodyMedium)

                Spacer(Modifier.height(20.dp))
                SectionLabel("Weather")
                GlassCard(modifier = Modifier.fillMaxWidth(), corner = 16.dp) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.WbCloudy, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Weather placeholder — connect a weather API later.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (place.photoUrls.size > 1) {
                    Spacer(Modifier.height(20.dp))
                    SectionLabel("Gallery")
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        place.photoUrls.forEach { u ->
                            AsyncImage(
                                model = u,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            )
                        }
                    }
                }

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
                            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

private val ColorTransparent = androidx.compose.ui.graphics.Color.Transparent
