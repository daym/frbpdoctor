package com.friendly_machines.frbpdoctor.ui.health

import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchListener
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchRawResponse
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.WatchCommunicationClientShorthand
import com.friendly_machines.frbpdoctor.databinding.ActivityHealthBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder


class HealthActivity : AppCompatActivity(), IWatchListener {
    companion object {
        const val TAG: String = "HealthActivity"
    }

    private lateinit var handler: Handler
    private lateinit var binding: ActivityHealthBinding
//    private var appBarConfiguration: AppBarConfiguration? = null

    private var serviceConnection: ServiceConnection? = null

    override fun onStart() {
        super.onStart()
        this.serviceConnection = WatchCommunicationClientShorthand.bindPeriodic(handler, 10000, this, this) { binder ->
            (binding.viewPager.adapter as HealthViewPagerAdapter).requestData(binding.viewPager.currentItem, binder)
        }
    }

    override fun onStop() {
        handler.removeCallbacksAndMessages(null)
        serviceConnection?.let {
            unbindService(it)
            serviceConnection = null
        }
        super.onStop()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val toolbar: Toolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar)

        binding = ActivityHealthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_activity_health)
        //setupActionBarWithNavController(this, navController)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)
        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        val adapter = HealthViewPagerAdapter(this)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(
            binding.tabs, binding.viewPager
        ) { tab: TabLayout.Tab, position: Int -> tab.text = adapter.getTabTitle(position) }.attach()

        val fab: FloatingActionButton = binding.fab

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        handler = Handler(Looper.getMainLooper())
    }

    // FIXME up
//    override fun onSupportNavigateUp(): Boolean {
//        return NavigationUI.navigateUp(Navigation.findNavController(this, R.id.nav_host_fragment_activity_health), appBarConfiguration)
//                || super.onSupportNavigateUp();
//    }

    private val bigBuffers = HashMap<Short, ByteArrayOutputStream>()
    private fun onBigWatchResponse(response: com.friendly_machines.fr_yhe_med.WatchBigResponseMed) {
        Log.d(TAG, "-> big decoded: $response")
        when (response) {
            is com.friendly_machines.fr_yhe_med.WatchBigResponseMed.GetSleepData -> {
                for (fragment in supportFragmentManager.fragments) {
                    if (fragment is SleepFragment) {
                        fragment.setData(response.data)
                    }
                }
            }

            is com.friendly_machines.fr_yhe_med.WatchBigResponseMed.GetHeatData -> {
                for (fragment in supportFragmentManager.fragments) {
                    if (fragment is HeatFragment) {
                        fragment.setData(response.data)
                    }
                }
            }

            is com.friendly_machines.fr_yhe_med.WatchBigResponseMed.GetStepData -> {
                for (fragment in supportFragmentManager.fragments) {
                    if (fragment is StepsFragment) {
                        fragment.setData(response.data)
                    }
                }
            }

            is com.friendly_machines.fr_yhe_med.WatchBigResponseMed.GetBpData -> {
                for (fragment in supportFragmentManager.fragments) {
                    if (fragment is BloodPressureFragment) {
                        fragment.setData(response.data)
                    }
                }
            }

            is com.friendly_machines.fr_yhe_med.WatchBigResponseMed.GetAlarm -> {
                for (fragment in supportFragmentManager.fragments) {
                    if (fragment is AlarmFragment) {
                        fragment.setData(response.data)
                    }
                }
            }

            is com.friendly_machines.fr_yhe_med.WatchBigResponseMed.GetSportData -> {
                for (fragment in supportFragmentManager.fragments) {
                    if (fragment is SportFragment) {
                        fragment.setData(response.data)
                    }
                }
            }

            else -> {

            }
        }
    }

    private fun bigAreWeDone(rawResponse: WatchRawResponse): Boolean {
        return rawResponse.arguments.isEmpty()
    }

    override fun onBigWatchRawResponse(rawResponse: WatchRawResponse) {
        val command = rawResponse.command
        // FIXME make sure the sequenceNumber are consecutive
        if (command == com.friendly_machines.fr_yhe_med.WatchOperation.DeviceInfo.code) {
            // TODO maybe handle the remainder here
            val buffer = bigBuffers[command]
            buffer?.let {
                bigBuffers[command] = ByteArrayOutputStream()
            }
        }
        if (bigAreWeDone(rawResponse)) {
            var buffer = bigBuffers[command]
            if (buffer == null) {
                buffer = ByteArrayOutputStream()
            }

            bigBuffers[command] = ByteArrayOutputStream()
            try {
                val response = com.friendly_machines.fr_yhe_med.WatchBigResponseMed.parse(
                    command, ByteBuffer.wrap(buffer.toByteArray()).order(
                        ByteOrder.BIG_ENDIAN
                    )
                )
                onBigWatchResponse(response)
            } catch (e: RuntimeException) {
                Log.d(TAG, "Parse error while parsing ${buffer.toByteArray()}: $e")
            }
        } else {
            if (command == com.friendly_machines.fr_yhe_med.WatchBigResponseMed.RAW_BLOOD_PRESSURE) {
                if (rawResponse.arguments.size == 4 + 4 + 1 + 1 + 4) {
                    val buf = ByteBuffer.wrap(rawResponse.arguments).order(ByteOrder.BIG_ENDIAN)
                    if (buf.get() == 64.toByte() && buf.get() == 64.toByte() && buf.get() == 64.toByte() && buf.get() == 64.toByte()) {
                        val id = buf.int
                        val systolicPressure = buf.get()
                        val diastolicPressure = buf.get()
                        val time = buf.int
                        // TODO: also remember those?
                    }
                    return
                } // else adds it to buffer below
            }

            var buffer = bigBuffers[command]
            if (buffer == null) {
                buffer = ByteArrayOutputStream()
                bigBuffers[command] = buffer
            }
            buffer.write(rawResponse.arguments)
        }
    }

    override fun onException(exception: Throwable) {
    }
}