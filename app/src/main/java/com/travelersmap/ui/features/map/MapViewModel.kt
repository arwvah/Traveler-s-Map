package com.travelersmap.ui.features.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelersmap.domain.model.CountryConfig
import com.travelersmap.domain.model.TouristPlace
import com.travelersmap.domain.repository.FavoriteRepository
import com.travelersmap.domain.repository.PlaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class MapUiState(
    /** Full tourist catalog for map pins (always country-scoped). */
    val allPlaces: List<TouristPlace> = emptyList(),
    /** Search-filtered list (same as allPlaces when query blank). */
    val places: List<TouristPlace> = emptyList(),
    val query: String = "",
    val selectedId: String? = null,
    val favoriteIds: Set<String> = emptySet(),
    val country: CountryConfig = CountryConfig.Uzbekistan,
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MapViewModel @Inject constructor(
    places: PlaceRepository,
    favorites: FavoriteRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selected = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MapUiState> = combine(
        places.observePlaces(),
        query.flatMapLatest { q -> places.search(q) },
        query,
        selected,
        favorites.observeFavoriteIds()
    ) { all, filtered, q, sel, favs ->
        MapUiState(
            allPlaces = all,
            places = filtered,
            query = q,
            selectedId = sel,
            favoriteIds = favs,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MapUiState())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun select(id: String?) {
        selected.value = id
    }
}
