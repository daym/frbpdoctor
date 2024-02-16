package com.friendly_machines.frbpdoctor.ui.home

//import io.reactivex.android.schedulers.AndroidSchedulers

import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchListener
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchRawResponse
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.WatchCommunicationClientShorthand
import com.friendly_machines.frbpdoctor.databinding.ActivityMainBinding
import com.friendly_machines.frbpdoctor.ui.customization.CustomizationActivity
import com.friendly_machines.frbpdoctor.ui.health.HealthActivity
import com.friendly_machines.frbpdoctor.ui.settings.SettingsActivity
import com.friendly_machines.frbpdoctor.ui.weather.WeatherActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity(), IWatchListener {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var handler: Handler
    private lateinit var binding: ActivityMainBinding

    companion object {
        const val TAG: String = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // FIXME setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        //val navController = findNavController(R.id.nav_host_fragment_activity_main)
        //setupActionBarWithNavController(this, navController)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        //NavigationUI.setupWithNavController(bottomNavigationView, navController)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.healthActivity -> {
                    startActivity(Intent(this, HealthActivity::class.java))
                    true
                }
                R.id.weatherActivity -> {
                    startActivity(Intent(this, WeatherActivity::class.java))
                    true
                }
                R.id.customizationActivity -> {
                    startActivity(Intent(this, CustomizationActivity::class.java))
                    true
                }
                // Add more menu items as needed
                else -> false
            }
        }

        val actionBarDrawerToggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        handler = Handler(Looper.getMainLooper())
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        // Just to make sure
        serviceConnection?.let {
            unbindService(it)
            serviceConnection = null
        }
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    drawerLayout.openDrawer(GravityCompat.START)
                }
                true
            }
            R.id.action_settings -> {
                val settingsIntent = Intent(this, SettingsActivity::class.java)
                // close all other activities.
                settingsIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(settingsIntent)
                // Make sure to close the MainActivity so that the WatchCommunicationService can die. That's important so that the connection to the bluetooth watch is closed so that we can actually find the watch when scanning, and so that we can connect to a new watch if necessary.
                finish()
                true
            }
            // Handle other menu items as needed
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private var serviceConnection: ServiceConnection? = null

    override fun onStart() {
        super.onStart()
        this.serviceConnection = WatchCommunicationClientShorthand.bindPeriodic(handler, 1000, this, this) { binder ->
            //binder.setTime()
            //binder.getBatteryState()
            //binder.getWatchFace() // ok but response is weird
        }
        // nope. startService(serviceIntent)
    }

    override fun onStop() {
        handler.removeCallbacksAndMessages(null)
        serviceConnection?.let {
            unbindService(it)
            serviceConnection = null
        }
        super.onStop()
    }

    override fun onBigWatchRawResponse(rawResponse: WatchRawResponse) {
        Log.d(TAG, "MainActivity: big watch raw response $rawResponse")
    }

    override fun onException(exception: Throwable) {
    }
}