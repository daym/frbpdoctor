package com.friendly_machines.frbpdoctor

import android.app.Application
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.polidea.rxandroidble3.LogConstants
import com.polidea.rxandroidble3.LogOptions
import com.polidea.rxandroidble3.RxBleClient

class MyApplication : Application() {
    companion object {
        lateinit var rxBleClient: RxBleClient
            private set
    }

    private fun tryEnableNotifications() {
        if (!NotificationManagerCompat.getEnabledListenerPackages(this).contains(this.packageName)) {
            //Toast.makeText(this, "Do you want to redirect notifications to ", Toast.LENGTH_LONG).show()
            // TODO remember forever when rejected
            // Direct the user to the settings where he can enable the notification listener
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        rxBleClient = RxBleClient.create(this)
        RxBleClient.updateLogOptions(
            LogOptions.Builder().setLogLevel(LogConstants.INFO)
                .build()
        )

        tryEnableNotifications()
    }
}