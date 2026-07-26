package com.travelersmap.ui.features.map

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.travelersmap.domain.model.TouristPlace

/**
 * Clusterable tourist POI. Only catalog places appear on the map — never generic businesses.
 */
data class PlaceClusterItem(
    val place: TouristPlace
) : ClusterItem {
    private val position = LatLng(place.latitude, place.longitude)

    override fun getPosition(): LatLng = position
    override fun getTitle(): String = place.name
    override fun getSnippet(): String = place.city
    override fun getZIndex(): Float = 1f
}
