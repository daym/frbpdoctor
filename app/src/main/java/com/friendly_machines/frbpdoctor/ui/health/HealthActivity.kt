package com.friendly_machines.frbpdoctor.ui.health

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.databinding.ActivityHealthBinding
import com.friendly_machines.frbpdoctor.logger.Logger
import com.friendly_machines.frbpdoctor.service.WatchCommunicationRawResponse
import com.friendly_machines.frbpdoctor.service.WatchCommunicationService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder


class HealthActivity : AppCompatActivity(), WatchCommunicationService.WatchCommunicationServiceListener {
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

    private val serviceConnection = object : ServiceConnection {
        private var disconnector: WatchCommunicationService? = null
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as WatchCommunicationService.WatchCommunicationServiceBinder
            disconnector = binder.addListener(this@HealthActivity)
            val periodicTask: Runnable = object : Runnable {
                override fun run() {
                    binder.getBpData()
                    binder.getSleepData()
                    binder.getStepData()
                    binder.getHeatData()
                    binder.getSportData()
                    // TODO WatchCommand.CurrentStep
                    // TODO WatchCommand.CurrentHeat

                    handler.postDelayed(this, 10000 /* ms */)
                }
            }
            handler.postDelayed(periodicTask, 10000 /* ms */);
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            handler.removeCallbacksAndMessages(null);
            disconnector!!.removeListener(this@HealthActivity)
        }
    }

    private var shouldUnbindService = false

    override fun onStart() {
        super.onStart()
        val serviceIntent = Intent(this, WatchCommunicationService::class.java)
        if (bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)) {
            shouldUnbindService = true;
        } else {
            Log.e(TAG, "Could not bind to WatchCommunicationService")
        }
        // nope. startService(serviceIntent)
    }

    override fun onStop() {
        handler.removeCallbacksAndMessages(null);
        if (shouldUnbindService) {
            unbindService(serviceConnection)
            shouldUnbindService = false;
        }
        super.onStop()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null);

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

        val hvpa = HealthViewPagerAdapter(this)
        binding.viewPager.adapter = hvpa
        TabLayoutMediator(
            binding.tabs, binding.viewPager
        ) { tab: TabLayout.Tab, position: Int -> tab.text = hvpa.getTabTitle(position) }.attach()

        val fab: FloatingActionButton = binding.fab

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        handler = Handler(Looper.getMainLooper());

    }

    // FIXME up
//    override fun onSupportNavigateUp(): Boolean {
//        return NavigationUI.navigateUp(Navigation.findNavController(this, R.id.nav_host_fragment_activity_health), appBarConfiguration)
//                || super.onSupportNavigateUp();
//    }

    private val bigBuffers = HashMap<Short, ByteArrayOutputStream>()
    private fun onBigWatchResponse(response: WatchCommunicationService.WatchResponse) {
        Logger.log("-> big decodedxx: $response")
        when (response) {
            is WatchCommunicationService.WatchResponse.SleepData -> {
                for (fragment in supportFragmentManager.fragments) {
                    if (fragment is SleepFragment) {
                        fragment.setData(response.data)
                    }
                }
            }
            is WatchCommunicationService.WatchResponse.HeatData -> {
                for (fragment in supportFragmentManager.fragments) {
                    if (fragment is HeatFragment) {
                        fragment.setData(response.data)
                    }
                }
            }
            is WatchCommunicationService.WatchResponse.StepData -> {
                for (fragment in supportFragmentManager.fragments) {
                    if (fragment is StepsFragment) {
                        fragment.setData(response.data)
                    }
                }
            }
            is WatchCommunicationService.WatchResponse.BpData -> {
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

    override fun onWatchResponse(response: WatchCommunicationService.WatchResponse) {
    }

    override fun onBigWatchRawResponse(rawResponse: WatchCommunicationRawResponse) {
        val command = rawResponse.command
        // FIXME make sure the sn are consecutive
        if (rawResponse.arguments.isEmpty()) { // we are done
            val buffer = bigBuffers.get(command)
            buffer?.let {
                bigBuffers.put(command, ByteArrayOutputStream())
                val response = WatchCommunicationService.WatchResponse.parse(
                    rawResponse.command, ByteBuffer.wrap(buffer.toByteArray()).order(
                        ByteOrder.BIG_ENDIAN
                    )
                )
                onBigWatchResponse(response)
            }
        } else {
            var buffer = bigBuffers.get(command)
            if (buffer == null) {
                buffer = ByteArrayOutputStream()
                bigBuffers.put(command, buffer)
            }
            buffer!!.write(rawResponse.arguments)
        }
    }
}