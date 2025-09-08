package com.friendly_machines.frbpdoctor.ui.settings

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.friendly_machines.frbpdoctor.BluetoothPermissionHandler

class SettingsActivity : AppCompatActivity() {
    private val BLUETOOTH_PERMISSION_REQUEST_CODE: Int = 0x100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Just in case the user clicks anything.
        BluetoothPermissionHandler.start(this, BLUETOOTH_PERMISSION_REQUEST_CODE) {
        }
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, SettingsFragment())
            .commit()
    }
}
