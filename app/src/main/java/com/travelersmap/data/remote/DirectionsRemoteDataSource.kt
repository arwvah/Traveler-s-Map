package com.travelersmap.data.remote

import com.travelersmap.domain.model.LatLngPoint
import com.travelersmap.domain.model.RouteEstimate
import com.travelersmap.domain.model.TravelMode
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Singleton
class DirectionsRemoteDataSource @Inject constructor(
    private val client: OkHttpClient
) {
    /**
     * Prefer Google Directions when a real Maps key is configured; otherwise haversine fallback
     * with a simple two-point polyline (still shows route on map + opens Google Maps navigation).
     */
    fun route(
        fromLat: Double,
        fromLng: Double,
        toLat: Double,
        toLng: Double,
        mode: TravelMode,
        mapsApiKey: String?
    ): RouteEstimate {
        val key = mapsApiKey?.takeIf {
            it.isNotBlank() && !it.contains("YOUR_GOOGLE") && it != "YOUR_GOOGLE_MAPS_API_KEY"
        }
        if (key != null) {
            runCatching {
                return fetchGoogle(fromLat, fromLng, toLat, toLng, mode, key)
            }
        }
        return offlineEstimate(fromLat, fromLng, toLat, toLng, mode)
    }

    private fun fetchGoogle(
        fromLat: Double,
        fromLng: Double,
        toLat: Double,
        toLng: Double,
        mode: TravelMode,
        key: String
    ): RouteEstimate {
        val travelMode = when (mode) {
            TravelMode.WALKING -> "walking"
            TravelMode.DRIVING -> "driving"
            TravelMode.CYCLING -> "bicycling"
            TravelMode.TRANSIT -> "transit"
        }
        val url = "https://maps.googleapis.com/maps/api/directions/json".toHttpUrl().newBuilder()
            .addQueryParameter("origin", "$fromLat,$fromLng")
            .addQueryParameter("destination", "$toLat,$toLng")
            .addQueryParameter("mode", travelMode)
            .addQueryParameter("key", key)
            .build()
        val request = Request.Builder().url(url).get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Directions HTTP ${response.code}")
            val json = JSONObject(response.body?.string().orEmpty())
            if (json.optString("status") != "OK") {
                error("Directions status ${json.optString("status")}")
            }
            val route = json.getJSONArray("routes").getJSONObject(0)
            val leg = route.getJSONArray("legs").getJSONObject(0)
            val distanceM = leg.getJSONObject("distance").getInt("value")
            val durationS = leg.getJSONObject("duration").getInt("value")
            val points = decodePolyline(
                route.getJSONObject("overview_polyline").getString("points")
            )
            return RouteEstimate(
                mode = mode,
                distanceKm = distanceM / 1000.0,
                durationMinutes = (durationS / 60).coerceAtLeast(1),
                isPlaceholder = false,
                polyline = points
            )
        }
    }

    fun offlineEstimate(
        fromLat: Double,
        fromLng: Double,
        toLat: Double,
        toLng: Double,
        mode: TravelMode
    ): RouteEstimate {
        val km = haversineKm(fromLat, fromLng, toLat, toLng)
        val road = km * 1.25
        val minutes = when (mode) {
            TravelMode.WALKING -> (road / 5.0 * 60).toInt()
            TravelMode.DRIVING -> (road / 55.0 * 60).toInt()
            TravelMode.CYCLING -> (road / 15.0 * 60).toInt()
            TravelMode.TRANSIT -> (road / 40.0 * 60).toInt()
        }.coerceAtLeast(1)
        return RouteEstimate(
            mode = mode,
            distanceKm = road,
            durationMinutes = minutes,
            isPlaceholder = mode == TravelMode.TRANSIT,
            polyline = listOf(
                LatLngPoint(fromLat, fromLng),
                LatLngPoint(toLat, toLng)
            )
        )
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    private fun decodePolyline(encoded: String): List<LatLngPoint> {
        val poly = ArrayList<LatLngPoint>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            poly.add(LatLngPoint(lat / 1e5, lng / 1e5))
        }
        return poly
    }
}
