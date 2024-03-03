package com.friendly_machines.frbpdoctor.online.weather

data class WeatherResponseDaily(
    val time: List<Long>,
    val rain_sum: List<Double>,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>,
    val showers_sum: List<Double>,
    val weather_code: List<Int>,
    val wind_speed_10m_max: List<Double>
)
