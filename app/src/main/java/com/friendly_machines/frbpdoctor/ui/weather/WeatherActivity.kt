package com.friendly_machines.frbpdoctor.ui.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponseType
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.WatchCommunicationClientShorthand
import com.friendly_machines.frbpdoctor.online.weather.WeatherResponse
import com.friendly_machines.frbpdoctor.online.weather.WeatherRetrofitClient
import com.friendly_machines.frbpdoctor.online.weather.WeatherTimeFormat
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.TimeZone

class WeatherActivity : AppCompatActivity() {
    private fun hasPermissions(context: Context, permissions: Set<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    private suspend fun requestPermissions(permissions: Set<String>): Map<String, Boolean> {
        val requestPermissionActivityContract = ActivityResultContracts.RequestMultiplePermissions()
        if (hasPermissions(this, permissions)) {
            return permissions.associateWith { true }
        }
        return suspendCancellableCoroutine { continuation ->
            val launcher = registerForActivityResult(requestPermissionActivityContract) { granted ->
                continuation.resume(
                    //val shouldShowRationale = currentActivity.shouldShowRequestPermissionRationale(it)
                    //PermissionDenied(shouldShowRationale)
                    granted // .containsAll(permissions)
                ) {
                    // FIXME cancellation cleanup
                }
            }
            launcher.launch(permissions.toTypedArray())
            continuation.invokeOnCancellation {
                launcher.unregister()
            }
        }
    }

    private fun sendWeatherToWatch(weather: WeatherResponse) {
        WatchCommunicationClientShorthand.bindExecOneCommandUnbind(this@WeatherActivity, WatchResponseType.SetWeather) { binder ->
            //                    val latitude = weather.latitude
            //                    val longitude = weather.longitude
            val timezone = weather.timezone
            //weather.daily.rain_sum
            val unixTimestamp = weather.daily.time[0]
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = unixTimestamp * 1000 // Convert seconds to milliseconds
            val month = calendar.get(Calendar.MONTH) + 1 // Months are 0-based, so we add 1
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val dayOfWeekMondayBased = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Convert to Monday-based (0-indexed) // FIXME
            val dummy = 0.toByte() // FIXME
            val temp = weather.current.temperature_2m.toInt().toByte()
            val location = timezone // "latitude: $latitude, longitude: $longitude"
            binder.setWeather(weatherType = weather.daily.weather_code[0], temp = temp, maxTemp = weather.daily.temperature_2m_max[0].toInt().toByte(), minTemp = weather.daily.temperature_2m_min[0].toInt().toByte(), dummy = dummy, month = month.toByte(), dayOfMonth = dayOfMonth.toByte(), dayOfWeekMondayBased = dayOfWeekMondayBased.toByte(), location = location)

            // TODO set tomorrow's weather
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val navController = findNavController(R.id.nav_host_fragment_activity_weather)
        //setupActionBarWithNavController(this, navController)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)
        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        lifecycleScope.launch {
            if (requestPermissions(setOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION)).values.all { it }) {
                val locationClient = LocationServices.getFusedLocationProviderClient(this@WeatherActivity)
                val locationTask = locationClient.lastLocation
                val location = locationTask.await()
                val weather = getWeatherFromOnline(location)
                sendWeatherToWatch(weather)
            } else {
                //throw RuntimeException("no permissions to get GPS location")
            }
        }
    }

    private suspend fun getWeatherFromOnline(location: Location): WeatherResponse {
        return withContext(Dispatchers.IO) {
            val timezoneString = TimeZone.getDefault().id
            WeatherRetrofitClient.instance.getWeather(latitude = location.latitude, longitude = location.longitude, elevation = location.altitude, timeFormat = WeatherTimeFormat.UNIXTIME, timezone = timezoneString, forecastDays = 2, current = listOf("temperature_2m"), daily = listOf("rain_sum", "temperature_2m_max", "temperature_2m_min", "showers_sum", "weather_code", "wind_speed_10m_max", "rain_sum"))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        // onBackPressed()
        return true
    }

}