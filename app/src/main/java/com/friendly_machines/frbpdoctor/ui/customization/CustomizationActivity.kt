package com.friendly_machines.frbpdoctor.ui.customization

import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchListener
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_med.WatchBigResponseMed
import com.friendly_machines.fr_yhe_pro.command.WatchWGetWatchDialInfoCommand
import com.friendly_machines.frbpdoctor.MedBigResponseBuffer
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.WatchCommunicationClientShorthand
import com.friendly_machines.frbpdoctor.databinding.ActivityCustomizationBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class CustomizationActivity : AppCompatActivity(), IWatchListener, MedBigResponseBuffer.IBigResponseListener {
    companion object {
        const val TAG: String = "CustomizationActivity"
    }

    private lateinit var handler: Handler
    private lateinit var binding: ActivityCustomizationBinding

    private var serviceConnection: ServiceConnection? = null

    override fun onStart() {
        super.onStart()
        this.serviceConnection = WatchCommunicationClientShorthand.bindPeriodic(handler, 10000, this, this) { binder ->
            (binding.viewPager.adapter as CustomizationViewPagerAdapter).requestData(binding.viewPager.currentItem, binder)
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
        bigBuffers.listener = this
        binding = ActivityCustomizationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val navController = findNavController(R.id.nav_host_fragment_activity_customization)
        //setupActionBarWithNavController(this, navController)
//        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)
//        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        val adapter = CustomizationViewPagerAdapter(this)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(
            binding.tabs, binding.viewPager
        ) { tab: TabLayout.Tab, position: Int -> tab.text = adapter.getTabTitle(position) }.attach()

        val fab: FloatingActionButton = binding.fab

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show()

        }

        handler = Handler(Looper.getMainLooper())
    }

    private val bigBuffers = MedBigResponseBuffer()
    override fun onBigWatchResponse(response: WatchBigResponseMed) {
        Log.d(TAG, "-> big decoded: $response")
        when (response) {
            is com.friendly_machines.fr_yhe_med.WatchBigResponseMed.GetAlarm -> {
                supportFragmentManager.fragments.filterIsInstance<AlarmFragment>().forEach { fragment ->
                    fragment.setData(response.data)
                }
            }
            // FIXME
//            is com.friendly_machines.fr_yhe_med.WatchBigResponseMed.GetWatchDial -> {
//                supportFragmentManager.fragments.filterIsInstance<WatchDialFragment>().forEach { fragment ->
//                    fragment.setData(response.data)
//                }
//            }
//
            else -> {

            }
        }
    }

    override fun onWatchResponse(response: WatchResponse) {
        super.onWatchResponse(response)
        when (response) {
            // = Pro =

            is WatchWGetWatchDialInfoCommand.Response -> {
                supportFragmentManager.fragments.filterIsInstance<WatchDialFragment>().forEach { fragment ->
                    fragment.setData(response.items)
                }
            }
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        finish()
        // onBackPressed()
        return true
    }

}