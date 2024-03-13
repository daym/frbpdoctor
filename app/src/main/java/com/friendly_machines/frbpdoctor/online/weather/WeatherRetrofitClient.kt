package com.friendly_machines.frbpdoctor.online.weather

import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WeatherRetrofitClient {
    //private const val BASE_URL = "https://localhost/v1/"
    private const val BASE_URL = "https://api.open-meteo.com/v1/"

    val instance: WeatherService by lazy {
        //var logging = HttpLoggingInterceptor()
        //logging.setLevel(HttpLoggingInterceptor.Level.BODY)

//        var client = OkHttpClient.Builder()
//            .connectionSpecs(listOf(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS))
//            //.addInterceptor(logging)
//            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
//            .client(client)
            .build()

        retrofit.create(WeatherService::class.java)
    }
}
