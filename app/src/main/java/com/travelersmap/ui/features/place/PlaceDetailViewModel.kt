package com.travelersmap.ui.features.place

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelersmap.domain.model.TouristPlace
import com.travelersmap.domain.repository.FavoriteRepository
import com.travelersmap.domain.repository.PlaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaceDetailUiState(
    val place: TouristPlace? = null,
    val nearby: List<TouristPlace> = emptyList(),
    val isFavorite: Boolean = false,
    val loading: Boolean = true
)

@HiltViewModel
class PlaceDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val places: PlaceRepository,
    private val favorites: FavoriteRepository
) : ViewModel() {

    private val placeId: String = checkNotNull(savedStateHandle["placeId"])

    val uiState: StateFlow<PlaceDetailUiState> = combine(
        places.observePlace(placeId),
        favorites.observeFavoriteIds(),
        places.observePlaces()
    ) { place, favIds, all ->
        val nearby = place?.nearbyIds
            ?.mapNotNull { id -> all.find { it.id == id } }
            .orEmpty()
        PlaceDetailUiState(
            place = place,
            nearby = nearby,
            isFavorite = placeId in favIds,
            loading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlaceDetailUiState())

    fun toggleFavorite() {
        viewModelScope.launch { favorites.toggle(placeId) }
    }
}
