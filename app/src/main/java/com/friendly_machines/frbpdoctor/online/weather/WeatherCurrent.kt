package com.friendly_machines.frbpdoctor.online.weather

data class WeatherCurrent(
    val time: Long,
    val interval: Int,
    val temperature_2m: Double
)