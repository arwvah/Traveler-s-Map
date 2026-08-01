package com.travelersmap.ui.features.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelersmap.data.local.RecentSearchDao
import com.travelersmap.data.local.RecentSearchEntity
import com.travelersmap.data.location.LocationTracker
import com.travelersmap.domain.model.CountryConfig
import com.travelersmap.domain.model.TouristPlace
import com.travelersmap.domain.model.UserLocation
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val allPlaces: List<TouristPlace> = emptyList(),
    val places: List<TouristPlace> = emptyList(),
    val query: String = "",
    val selectedId: String? = null,
    val favoriteIds: Set<String> = emptySet(),
    val country: CountryConfig = CountryConfig.Uzbekistan,
    val isLoading: Boolean = true,
    val userLocation: UserLocation? = null,
    val locationPermissionGranted: Boolean = false,
    val centerOnUserToken: Int = 0,
    val recentSearches: List<String> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MapViewModel @Inject constructor(
    places: PlaceRepository,
    favorites: FavoriteRepository,
    private val recentSearchDao: RecentSearchDao,
    private val locationTracker: LocationTracker
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selected = MutableStateFlow<String?>(null)
    private val locationGranted = MutableStateFlow(locationTracker.hasPermission())
    private val userLocation = MutableStateFlow<UserLocation?>(null)
    private val centerToken = MutableStateFlow(0)

    private data class CatalogSlice(
        val all: List<TouristPlace>,
        val filtered: List<TouristPlace>,
        val q: String,
        val sel: String?,
        val favs: Set<String>,
        val recent: List<String>
    )

    private data class LocationSlice(
        val granted: Boolean,
        val loc: UserLocation?,
        val token: Int
    )

    private val catalogCore = combine(
        places.observePlaces(),
        query.flatMapLatest { q -> places.search(q) },
        query,
        selected,
        favorites.observeFavoriteIds()
    ) { all, filtered, q, sel, favs ->
        CatalogSlice(all, filtered, q, sel, favs, emptyList())
    }

    private val catalog = combine(
        catalogCore,
        recentSearchDao.observeRecent(8)
    ) { core, recent ->
        core.copy(recent = recent.map { it.query })
    }

    private val locationSlice = combine(
        locationGranted,
        userLocation,
        centerToken
    ) { granted, loc, token ->
        LocationSlice(granted, loc, token)
    }

    val uiState: StateFlow<MapUiState> = combine(catalog, locationSlice) { cat, loc ->
        MapUiState(
            allPlaces = cat.all,
            places = cat.filtered,
            query = cat.q,
            selectedId = cat.sel,
            favoriteIds = cat.favs,
            isLoading = false,
            userLocation = loc.loc,
            locationPermissionGranted = loc.granted,
            centerOnUserToken = loc.token,
            recentSearches = cat.recent
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MapUiState())

    init {
        if (locationTracker.hasPermission()) {
            startLocationUpdates()
        }
    }

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun rememberSearch(value: String) {
        val q = value.trim()
        if (q.length < 2) return
        viewModelScope.launch {
            recentSearchDao.insert(RecentSearchEntity(query = q, searchedAt = System.currentTimeMillis()))
        }
    }

    fun select(id: String?) {
        selected.value = id
    }

    fun onPermissionResult(granted: Boolean) {
        locationGranted.value = granted
        if (granted) {
            startLocationUpdates()
            centerOnUser()
        }
    }

    fun centerOnUser() {
        centerToken.update { it + 1 }
        viewModelScope.launch {
            locationTracker.currentLocation()?.let { userLocation.value = it }
        }
    }

    private fun startLocationUpdates() {
        viewModelScope.launch {
            locationTracker.observeLocation().collect { loc ->
                if (loc != null) userLocation.value = loc
            }
        }
    }
}
