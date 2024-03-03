package com.friendly_machines.frbpdoctor.online.weather

data class WeatherResponseDailyUnits(
    val time: String,
    val rain_sum: String,
    val temperature_2m_max: String,
    val temperature_2m_min: String,
    val showers_sum: String,
    val weather_code: String,
    val wind_speed_10m_max: String,
)
