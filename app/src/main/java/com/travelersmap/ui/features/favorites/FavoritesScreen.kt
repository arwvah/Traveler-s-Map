package com.travelersmap.ui.features.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.travelersmap.domain.model.TouristPlace
import com.travelersmap.domain.repository.FavoriteRepository
import com.travelersmap.ui.components.EmptyState
import com.travelersmap.ui.components.PlaceMetaLine
import com.travelersmap.ui.components.RatingStars
import com.travelersmap.ui.theme.GlassCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    favorites: FavoriteRepository
) : ViewModel() {
    val places: StateFlow<List<TouristPlace>> = favorites.observeFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

@Composable
fun FavoritesScreen(
    onOpenPlace: (String) -> Unit,
    vm: FavoritesViewModel = hiltViewModel()
) {
    val places by vm.places.collectAsStateWithLifecycle()
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Text("Saved places", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Stored locally · no account required",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        if (places.isEmpty()) {
            EmptyState("No favorites yet. Open a place and tap Save.")
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                items(places, key = { it.id }) { place ->
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clickable { onOpenPlace(place.id) },
                        corner = 18.dp
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(place.name, style = MaterialTheme.typography.titleLarge)
                            PlaceMetaLine(place)
                            Spacer(Modifier.height(4.dp))
                            RatingStars(place.rating)
                        }
                    }
                }
            }
        }
    }
}
