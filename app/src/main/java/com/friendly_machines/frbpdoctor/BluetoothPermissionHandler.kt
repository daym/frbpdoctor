package com.friendly_machines.frbpdoctor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/** Since a service is not allowed to show the permission prompt directly, we have this helper class and all our activities have to call its functions. WTF.

TODO: Try removing BLUETOOTH_SCAN.
*/
object BluetoothPermissionHandler {
    private val pendingActions = mutableMapOf<Int, () -> Unit>()

    fun checkPermissions(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) == PackageManager.PERMISSION_GRANTED
    }

    public fun handlePermissionResult(
        activity: AppCompatActivity,
        requestCode: Int,
        grantResults: IntArray
    ) {
        pendingActions[requestCode]?.let { continuation ->
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                continuation()
            } else {
                Toast.makeText(activity, "Bluetooth permissions required", Toast.LENGTH_SHORT).show()
                // Will fail--but better than getting stuck.
                continuation()
            }
            pendingActions.remove(requestCode)
        }
    }

    fun start(activity: AppCompatActivity, requestCode: Int, continuation: () -> Unit) {
        if (checkPermissions(activity)) {
            continuation()
        } else {
            pendingActions[requestCode] = continuation
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                ),
                requestCode
            )
        }
    }
}