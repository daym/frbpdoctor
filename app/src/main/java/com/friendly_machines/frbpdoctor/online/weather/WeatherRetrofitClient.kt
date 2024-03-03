package com.friendly_machines.frbpdoctor.online.weather

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WeatherRetrofitClient {
    private const val BASE_URL = "https://api.open-meteo.com/v1/"

    val instance: WeatherService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(WeatherService::class.java)
    }
}
