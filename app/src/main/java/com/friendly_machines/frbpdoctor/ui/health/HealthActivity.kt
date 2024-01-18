package com.friendly_machines.frbpdoctor.ui.health

import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.WatchCommunicationServiceClientShorthand
import com.friendly_machines.frbpdoctor.databinding.ActivityHealthBinding
import com.friendly_machines.frbpdoctor.watchprotocol.bluetooth.WatchListener
import com.friendly_machines.frbpdoctor.watchprotocol.notification.WatchRawResponse
import com.friendly_machines.frbpdoctor.watchprotocol.notification.WatchResponse
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder


class HealthActivity : AppCompatActivity(), WatchListener {
    companion object {
        const val TAG: String = "HealthActivity"
    }

    private lateinit var handler: Handler
    private lateinit var binding: ActivityHealthBinding
//    private var appBarConfiguration: AppBarConfiguration? = null

    override fun onPause() {
        super.onPause()
        // TODO
    }

    private var serviceConnection: ServiceConnection? = null

    override fun onStart() {
        super.onStart()
        this.serviceConnection = WatchCommunicationServiceClientShorthand.bindPeriodic(handler, 10000, this, this) { binder ->
            binder.getBpData()
            binder.getSleepData()
            binder.getStepData()
            binder.getHeatData()
            binder.getSportData()
            // TODO WatchCommand.CurrentStep
            // TODO WatchCommand.CurrentHeat
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
    private fun onBigWatchResponse(response: WatchResponse) {
        Log.d(TAG, "-> big decoded: $response")
        when (response) {
            is WatchResponse.SleepData -> {
                for (fragment in supportFragmentManager.fragments) {
                    if (fragment is SleepFragment) {
                        fragment.setData(response.data)
                    }
                }
            }
            is WatchResponse.HeatData -> {
                for (fragment in supportFragmentManager.fragments) {
                    if (fragment is HeatFragment) {
                        fragment.setData(response.data)
                    }
                }
            }
            is WatchResponse.StepData -> {
                for (fragment in supportFragmentManager.fragments) {
                    if (fragment is StepsFragment) {
                        fragment.setData(response.data)
                    }
                }
            }
            is WatchResponse.BpData -> {
                for (fragment in supportFragmentManager.fragments) {
                    if (fragment is BloodPressureFragment) {
                        fragment.setData(response.data)
                    }
                }
            }
            else -> {

            }
        }
    }

    override fun onWatchResponse(response: WatchResponse) {
    }

    override fun onBigWatchRawResponse(rawResponse: WatchRawResponse) {
        val command = rawResponse.command
        // FIXME make sure the sequenceNumber are consecutive
        if (rawResponse.arguments.isEmpty()) { // we are done
            val buffer = bigBuffers[command]
            buffer?.let {
                bigBuffers[command] = ByteArrayOutputStream()
                val response = WatchResponse.parse(
                    rawResponse.command, ByteBuffer.wrap(buffer.toByteArray()).order(
                        ByteOrder.BIG_ENDIAN
                    )
                )
                onBigWatchResponse(response)
            }
        } else {
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