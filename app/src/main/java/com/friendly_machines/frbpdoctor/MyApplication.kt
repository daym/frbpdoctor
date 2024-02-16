package com.friendly_machines.frbpdoctor

import android.app.Application
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.friendly_machines.frbpdoctor.service.NotificationListener
import com.polidea.rxandroidble3.LogConstants
import com.polidea.rxandroidble3.LogOptions
import com.polidea.rxandroidble3.RxBleClient


class MyApplication : Application() {
    private var packageChangeReceiver: BroadcastReceiver? = null

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

    // You can catch the broadcast Intent.ACTION_PACKAGE_CHANGED to know when the service gets disabled.

    private fun toggleNotificationListenerService() {
        val pm = packageManager
        pm.setComponentEnabledSetting(ComponentName(this, NotificationListener::class.java), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        pm.setComponentEnabledSetting(ComponentName(this, NotificationListener::class.java), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
    }

    private fun isNotificationListenerServiceEnabled(context: Context): Boolean {
        val packageNames = NotificationManagerCompat.getEnabledListenerPackages(context)
        return packageNames.contains(context.packageName)
    }


    /** Sometimes the NotificationListener gets randomly deleted.
     * Re-enable it at least in the common case. */
    class PackageChangeReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (Intent.ACTION_PACKAGE_CHANGED == intent?.action) {
                // TODO toggleNotificationListenerService()
            }
        }
    }

    private fun registerPackageChangeReceiver() {
        packageChangeReceiver = PackageChangeReceiver()
        val filter = IntentFilter(Intent.ACTION_PACKAGE_CHANGED)
        registerReceiver(packageChangeReceiver, filter)
    }

//    fun accessZenSnoozingTime() {
//        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//        val policy = notificationManager.notificationPolicy
//        val startTime = policy.startTime
//        val endTime = policy.endTime
//    }

    override fun onCreate() {
        super.onCreate()
        rxBleClient = RxBleClient.create(this)
        RxBleClient.updateLogOptions(
            LogOptions.Builder().setLogLevel(LogConstants.INFO)
                .build()
        )

        tryEnableNotifications()
        registerPackageChangeReceiver()
    }

    override fun onTerminate() {
        unregisterReceiver(packageChangeReceiver)
        super.onTerminate()
    }
}