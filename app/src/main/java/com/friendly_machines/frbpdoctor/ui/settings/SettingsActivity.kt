package com.friendly_machines.frbpdoctor.ui.settings

import android.content.ServiceConnection
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.friendly_machines.frbpdoctor.WatchCommunicationServiceClientShorthand
import com.friendly_machines.frbpdoctor.service.WatchCommunicationService
import com.friendly_machines.frbpdoctor.watchprotocol.bluetooth.WatchListener
import com.friendly_machines.frbpdoctor.watchprotocol.notification.WatchResponse

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, SettingsFragment())
            .commit()
    }
}
