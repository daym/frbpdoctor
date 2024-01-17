package com.friendly_machines.frbpdoctor.ui.home

//import io.reactivex.android.schedulers.AndroidSchedulers

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.databinding.ActivityMainBinding
import com.friendly_machines.frbpdoctor.logger.Logger
import com.friendly_machines.frbpdoctor.service.WatchCommunicationService
import com.friendly_machines.frbpdoctor.ui.settings.SettingsActivity
import com.friendly_machines.frbpdoctor.watchprotocol.bluetooth.WatchListener
import com.friendly_machines.frbpdoctor.watchprotocol.notification.WatchRawResponse
import com.friendly_machines.frbpdoctor.watchprotocol.notification.WatchResponse
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity(),
    WatchListener {
    private lateinit var handler: Handler
    private lateinit var binding: ActivityMainBinding
    companion object {
        const val TAG: String = "MainActivity"
    }

//    private fun showToast(message: String) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // FIXME setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        //setupActionBarWithNavController(this, navController)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)
        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        handler = Handler(Looper.getMainLooper())
    }

    override fun onDestroy() {
        super.onDestroy()

        handler.removeCallbacksAndMessages(null)
        // Just to make sure
        if (shouldUnbindService) {
            unbindService(serviceConnection)
            shouldUnbindService = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val settingsIntent = Intent(this, SettingsActivity::class.java)
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

    override fun onPause() {
        super.onPause()
        // TODO
    }

    private val serviceConnection = object : ServiceConnection {
        private var disconnector: WatchCommunicationService? = null
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as WatchCommunicationService.WatchCommunicationServiceBinder
            disconnector = binder.addListener(this@MainActivity)
            val periodicTask: Runnable = object : Runnable {
                override fun run() {
                    binder.setTime()
                    binder.getBatteryState()
                    binder.getWatchFace() // ok but response is weird
                    binder.getAlarm() // big
                    handler.postDelayed(this, 1000 /* ms */)
                }
            }
            handler.postDelayed(periodicTask, 1000 /* ms */)
            // FIXME setAlarm WatchCommand.SetAlarm(0, 1, 1, 2, 3, 1, ByteArray(0)), // OK, status=0, doesn't actually add the alarm.
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            handler.removeCallbacksAndMessages(null)
            disconnector!!.removeListener(this@MainActivity)
        }
    }

    private var shouldUnbindService = false

    override fun onStart() {
        super.onStart()
        val serviceIntent = Intent(this, WatchCommunicationService::class.java)
        if (bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)) {
            shouldUnbindService = true
        } else {
            Log.e(TAG, "Could not bind to WatchCommunicationService")
        }
        // nope. startService(serviceIntent)
    }

    override fun onStop() {
        handler.removeCallbacksAndMessages(null)
        if (shouldUnbindService) {
            unbindService(serviceConnection)
            shouldUnbindService = false
        }
        super.onStop()
    }


    override fun onWatchResponse(response: WatchResponse) {
        Log.d("FrBpDoctor", response.toString())
        when (response) {
            is WatchResponse.DeviceInfo -> {

                response.romVersion // maybe lower 16 bits: protocol version: hi, lo
                response.soc
                response.protocolVersion
            }

            is WatchResponse.GetWatchFace -> {
                val clockView = supportFragmentManager.findFragmentById(R.id.clockView)

                response.count
                response.content
            }

            is WatchResponse.GetAlarm -> { // (big)
                val alarmTimeButton = supportFragmentManager.findFragmentById(R.id.alarmTimeButton)
                val alarmTimePicker = supportFragmentManager.findFragmentById(R.id.alarmTimePicker)

                response.successCount
                response.data
            }

            is WatchResponse.GetBatteryState -> {
                response.id
                response.voltage
            }

            else -> {
                // ignore
            }
        }
    }

    override fun onBigWatchRawResponse(response: WatchRawResponse) {
        // FIXME handle alarm maybe
        Logger.log(response.toString())
    }

    override fun onException(exception: Throwable) {
    }
}