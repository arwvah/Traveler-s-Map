package com.travelersmap.data.remote

import com.travelersmap.domain.model.WeatherInfo
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Open-Meteo free weather API — no API key required.
 */
@Singleton
class WeatherRemoteDataSource @Inject constructor(
    private val client: OkHttpClient
) {
    suspend fun fetch(lat: Double, lng: Double): WeatherInfo {
        val url = "https://api.open-meteo.com/v1/forecast".toHttpUrl().newBuilder()
            .addQueryParameter("latitude", lat.toString())
            .addQueryParameter("longitude", lng.toString())
            .addQueryParameter(
                "current",
                "temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m"
            )
            .addQueryParameter("daily", "weather_code,temperature_2m_max,temperature_2m_min")
            .addQueryParameter("timezone", "auto")
            .addQueryParameter("forecast_days", "1")
            .build()

        val request = Request.Builder().url(url).get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                error("Weather HTTP ${response.code}")
            }
            val body = response.body?.string().orEmpty()
            val json = JSONObject(body)
            val current = json.getJSONObject("current")
            val daily = json.getJSONObject("daily")
            val code = current.optInt("weather_code", 0)
            val high = daily.getJSONArray("temperature_2m_max").optDouble(0)
            val low = daily.getJSONArray("temperature_2m_min").optDouble(0)
            val dailyCode = daily.getJSONArray("weather_code").optInt(0, code)
            return WeatherInfo(
                temperatureC = current.optDouble("temperature_2m"),
                feelsLikeC = current.optDouble("apparent_temperature"),
                humidityPercent = current.optInt("relative_humidity_2m"),
                windSpeedKmh = current.optDouble("wind_speed_10m"),
                condition = weatherCodeLabel(code),
                conditionCode = code,
                todayHighC = high,
                todayLowC = low,
                todaySummary = "Today: ${weatherCodeLabel(dailyCode)}, high ${high.toInt()}° / low ${low.toInt()}°"
            )
        }
    }

    private fun weatherCodeLabel(code: Int): String = when (code) {
        0 -> "Clear sky"
        1, 2 -> "Partly cloudy"
        3 -> "Overcast"
        45, 48 -> "Fog"
        51, 53, 55 -> "Drizzle"
        61, 63, 65 -> "Rain"
        66, 67 -> "Freezing rain"
        71, 73, 75, 77 -> "Snow"
        80, 81, 82 -> "Rain showers"
        85, 86 -> "Snow showers"
        95 -> "Thunderstorm"
        96, 99 -> "Thunderstorm with hail"
        else -> "Mixed conditions"
    }
}
