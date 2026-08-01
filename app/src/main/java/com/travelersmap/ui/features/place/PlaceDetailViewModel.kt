package com.travelersmap.ui.features.place

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelersmap.domain.model.TouristPlace
import com.travelersmap.domain.model.WeatherInfo
import com.travelersmap.domain.repository.FavoriteRepository
import com.travelersmap.domain.repository.PlaceRepository
import com.travelersmap.domain.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaceDetailUiState(
    val place: TouristPlace? = null,
    val nearby: List<TouristPlace> = emptyList(),
    val isFavorite: Boolean = false,
    val loading: Boolean = true,
    val weather: WeatherInfo? = null,
    val weatherLoading: Boolean = false,
    val weatherError: String? = null
)

@HiltViewModel
class PlaceDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val places: PlaceRepository,
    private val favorites: FavoriteRepository,
    private val weatherRepo: WeatherRepository
) : ViewModel() {

    private val placeId: String = checkNotNull(savedStateHandle["placeId"])
    private val weatherState = MutableStateFlow<WeatherSlot>(WeatherSlot())

    private data class WeatherSlot(
        val weather: WeatherInfo? = null,
        val loading: Boolean = false,
        val error: String? = null
    )

    val uiState: StateFlow<PlaceDetailUiState> = combine(
        places.observePlace(placeId),
        favorites.observeFavoriteIds(),
        places.observePlaces(),
        weatherState
    ) { place, favIds, all, weather ->
        val nearby = place?.nearbyIds
            ?.mapNotNull { id -> all.find { it.id == id } }
            .orEmpty()
        PlaceDetailUiState(
            place = place,
            nearby = nearby,
            isFavorite = placeId in favIds,
            loading = false,
            weather = weather.weather,
            weatherLoading = weather.loading,
            weatherError = weather.error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlaceDetailUiState())

    init {
        viewModelScope.launch {
            places.markViewed(placeId)
            val place = places.getPlace(placeId) ?: return@launch
            weatherState.update { it.copy(loading = true, error = null) }
            runCatching { weatherRepo.weatherFor(place.latitude, place.longitude) }
                .onSuccess { info ->
                    weatherState.update { WeatherSlot(weather = info, loading = false) }
                }
                .onFailure { e ->
                    weatherState.update {
                        WeatherSlot(loading = false, error = e.message ?: "Weather unavailable")
                    }
                }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch { favorites.toggle(placeId) }
    }

    fun retryWeather() {
        val place = uiState.value.place ?: return
        viewModelScope.launch {
            weatherState.update { it.copy(loading = true, error = null) }
            runCatching { weatherRepo.weatherFor(place.latitude, place.longitude) }
                .onSuccess { info ->
                    weatherState.update { WeatherSlot(weather = info, loading = false) }
                }
                .onFailure { e ->
                    weatherState.update {
                        WeatherSlot(loading = false, error = e.message ?: "Weather unavailable")
                    }
                }
        }
    }
}
