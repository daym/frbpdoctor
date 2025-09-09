package com.friendly_machines.frbpdoctor.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.friendly_machines.frbpdoctor.service.WatchCommunicationService

abstract class BaseActivity : AppCompatActivity() {
    
    private val serviceStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == WatchCommunicationService.ACTION_SERVICE_STATUS) {
                val isRunning = intent.getBooleanExtra(WatchCommunicationService.EXTRA_RUNNING, false)
                updateSubtitle(isRunning)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Register broadcast receiver
        val filter = IntentFilter(WatchCommunicationService.ACTION_SERVICE_STATUS)
        
        // Android 14+ requires specifying if receiver is exported
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.registerReceiver(
                this,
                serviceStatusReceiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )
        } else {
            registerReceiver(serviceStatusReceiver, filter)
        }
        
        // Set initial subtitle based on current state
        updateSubtitle(WatchCommunicationService.isRunning)
    }
    
    override fun onResume() {
        super.onResume()
        // Update subtitle when returning to activity
        // This ensures correct state when navigating between activities
        updateSubtitle(WatchCommunicationService.isRunning)
    }
    
    override fun onDestroy() {
        unregisterReceiver(serviceStatusReceiver)
        super.onDestroy()
    }
    
    private fun updateSubtitle(isRunning: Boolean) {
        supportActionBar?.subtitle = if (isRunning) {
            "Watch Connected"
        } else {
            "Watch Disconnected"
        }
    }
}