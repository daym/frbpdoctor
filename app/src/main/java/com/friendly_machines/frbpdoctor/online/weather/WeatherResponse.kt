package com.friendly_machines.frbpdoctor.online.weather

data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val generationtime_ms: Double,
    val utc_offset_seconds: Int,
    val timezone: String,
    val timezone_abbreviation: String,
    val elevation: Double,
    val current_units: WeatherCurrentUnits,
    val current: WeatherCurrent,
    val daily_units: WeatherResponseDailyUnits,
    val daily: WeatherResponseDaily
)
