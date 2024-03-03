package com.friendly_machines.frbpdoctor.online.weather

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("elevation") elevation: Double? = null,
        @Query("current") current: List<String>?,
        @Query("daily") daily: List<String>?,
        @Query("timeformat") timeFormat: WeatherTimeFormat = WeatherTimeFormat.UNIXTIME,
        @Query("timezone") timezone: String = "auto", // example: Europe/Berlin
        @Query("forecast_days") forecastDays: Int?,
        @Query("temperature_unit") temperatureUnit: String = "celsius",
        @Query("wind_speed_unit") windSpeedUnit: String = "kmh",
        @Query("precipitation_unit") precipitationUnit: String = "mm",
    ): WeatherResponse
}
