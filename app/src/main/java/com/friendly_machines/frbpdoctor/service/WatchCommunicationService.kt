package com.friendly_machines.frbpdoctor.service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.IBinder
import android.telecom.TelecomManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchCommunicator
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchListener
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchPhoneCallControlAnswer
import com.friendly_machines.frbpdoctor.AppSettings
import com.friendly_machines.frbpdoctor.MyApplication
import com.friendly_machines.frbpdoctor.ui.settings.SettingsActivity
import java.security.MessageDigest
import kotlin.system.exitProcess

/** Note: This service will accept calls when user clicks the respective button on the watch. */
class WatchCommunicationService : Service(), IWatchListener {
    companion object {
        const val TAG: String = "WatchCommunicationService"
    }

    private var communicator: IWatchCommunicator? = null

    private fun showSetMandatorySettingsDialog() {
        val settingsIntent = Intent(this, SettingsActivity::class.java)
        settingsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(settingsIntent)
    }

    private fun areMandatorySettingsSet(): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        return AppSettings.areMandatorySettingsSet(sharedPreferences)
    }

    private fun die(message: String) {
        Log.e(TAG, message)
        showSetMandatorySettingsDialog()
        return
    }
    override fun onCreate() {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
            return die("No bluetooth LE support in the phone")
        if (!areMandatorySettingsSet())
            return die("settings missing")
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val key = AppSettings.getWatchKey(this, sharedPreferences)
        if (key == null)
            return die("Key was null")
        val keyDigest = MessageDigest.getInstance("MD5").digest(key)
        val watchMacAddress = sharedPreferences.getString(AppSettings.KEY_WATCH_MAC_ADDRESS, "")!!
        val watchCommunicatorClassname = sharedPreferences.getString(AppSettings.KEY_WATCH_COMMUNICATOR_CLASS, "")!!
        val bleDevice = MyApplication.rxBleClient.getBleDevice(watchMacAddress)
        if (watchCommunicatorClassname == "")
            return die("Unknown watch type")
        val communicator = classLoader.loadClass(watchCommunicatorClassname).newInstance() as IWatchCommunicator
        communicator.addListener(this)
        communicator.start(bleDevice, keyDigest)
        this.communicator = communicator
    }

    override fun onDestroy() {
        val communicator = this.communicator
        if (communicator != null) {
            communicator.removeListener(this)
            communicator.stop()
            this.communicator = null
        }
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
    }
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        Log.d(TAG, "Service started")
//        // onStartCommand can be called multiple times although the service is already running. Use this.connecting as a proxy for that.
//        return START_STICKY // TODO opportunistic
//    }


    override fun onBind(intent: Intent): IBinder? {
        val communicator = this.communicator
        if (communicator != null) {
            return communicator.binder
        } else {
            return null
        }
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        super.onUnbind(intent)
        return true // call onRebind
    }

    // TODO what if we are restarted: will the listener be restored?!

    fun removeListener(listener: IWatchListener) {
        communicator?.removeListener(listener)
    }

    private fun addListener(listener: IWatchListener): WatchCommunicationService {
        communicator?.addListener(listener)
        return this
    }

    private fun acceptIncomingCall() {
        val telecomManager = this.getSystemService(TELECOM_SERVICE) as TelecomManager
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.MODIFY_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        ) {
            // TODO use InCallService instead
            telecomManager.acceptRingingCall()
        }
    }

    override fun onWatchPhoneCallControl(answer: WatchPhoneCallControlAnswer) {
        Toast.makeText(this, "Got notification from watch: ${answer}", Toast.LENGTH_LONG).show()

        when (answer) {
            WatchPhoneCallControlAnswer.Accept -> acceptIncomingCall()
            WatchPhoneCallControlAnswer.Reject -> { // FIXME
            }
        }
    }

    override fun onResetSequenceNumbers() {
        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val key = AppSettings.getWatchKey(this, sharedPreferences)
        if (key != null) {
            // TODO use value corresponding to KEY_USER_ID
            // Note: If you say the exact right things, this will return status=0. Otherwise, it will never respond.
            communicator?.binder?.bindWatch(4711, key)
        }
    }

    override fun onMtuResponse(mtu: Int) {
        communicator?.binder?.resetSequenceNumbers()
    }

    override fun onException(exception: Throwable) {
        super.onException(exception)
        Log.e(TAG, "Error: $exception")
        Toast.makeText(this, "Error: $exception", Toast.LENGTH_LONG).show()
    }
}