package com.friendly_machines.frbpdoctor.service

import android.Manifest
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.RingtoneManager
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.net.Uri
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.telecom.TelecomManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchCommunicator
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchListener
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchCameraControlAnswer
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchMusicControlAnswer
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchPhoneCallControlAnswer
import com.friendly_machines.frbpdoctor.AppSettings
import com.friendly_machines.frbpdoctor.MyApplication
import com.friendly_machines.frbpdoctor.ui.camera.CameraActivity
import com.friendly_machines.frbpdoctor.ui.settings.SettingsActivity
import java.security.MessageDigest


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
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        showSetMandatorySettingsDialog()
        return
    }

    override fun onCreate() {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
            return die("No bluetooth LE support in the phone")
        if (!areMandatorySettingsSet())
            return die("settings missing")
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val key = AppSettings.getWatchKey(this, sharedPreferences) ?: return die("Key was null")
        val keyDigest = MessageDigest.getInstance("MD5").digest(key)
        val watchMacAddress = sharedPreferences.getString(AppSettings.KEY_WATCH_MAC_ADDRESS, "")!!
        val watchCommunicatorClassname = sharedPreferences.getString(AppSettings.KEY_WATCH_COMMUNICATOR_CLASS, "")!!
        val bleDevice = MyApplication.rxBleClient.getBleDevice(watchMacAddress)
        if (watchCommunicatorClassname == "")
            return die("Unknown watch type")

        val communicator = try {
            classLoader.loadClass(watchCommunicatorClassname).newInstance() as IWatchCommunicator
        } catch (e: ClassNotFoundException) {
            return die("Communicator wasn't found: $e")
        }
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
        return if (communicator != null) {
            communicator.binder
        } else {
            null
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

    override fun onWatchMusicControl(control: WatchMusicControlAnswer) {
        if (control == WatchMusicControlAnswer.IncreaseVolume || control == WatchMusicControlAnswer.DecreaseVolume) {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            when (control) {
                WatchMusicControlAnswer.IncreaseVolume -> audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND)
                WatchMusicControlAnswer.DecreaseVolume -> audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND)
                else -> {
                }
            }
        } else {
            // Older: Just val mediaController = MediaControllerCompat.getMediaController(this)
            val mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
            val mediaControllers = mediaSessionManager.getActiveSessions(ComponentName(this, NotificationListenerService::class.java))
            for (controller in mediaControllers) {
                if (controller.playbackState?.state == PlaybackState.STATE_PLAYING) {
                    when (control) {
                        WatchMusicControlAnswer.PlayPause -> controller.transportControls.pause()
                        WatchMusicControlAnswer.NextSong -> controller.transportControls.skipToNext()
                        WatchMusicControlAnswer.PreviousSong -> controller.transportControls.skipToPrevious()
                        WatchMusicControlAnswer.IncreaseVolume -> {
                        }

                        WatchMusicControlAnswer.DecreaseVolume -> {
                        }

                        WatchMusicControlAnswer.Unknown -> {
                        }
                    }
                    return
                }
            }
            // TODO: Prioritize
            for (controller in mediaControllers) {
                when (control) {
                    WatchMusicControlAnswer.PlayPause -> controller.transportControls.play()
                    WatchMusicControlAnswer.NextSong -> controller.transportControls.skipToNext()
                    WatchMusicControlAnswer.PreviousSong -> controller.transportControls.skipToPrevious()
                    WatchMusicControlAnswer.IncreaseVolume -> {
                    }

                    WatchMusicControlAnswer.DecreaseVolume -> {
                    }

                    WatchMusicControlAnswer.Unknown -> {
                    }
                }
                return
            }
        }
    }


    override fun onWatchCameraControl(control: WatchCameraControlAnswer) {
        val intent = Intent(this, CameraActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            action = when (control) {
                WatchCameraControlAnswer.Prepare -> CameraActivity.PREPARE_CAMERA
                WatchCameraControlAnswer.Shoot -> CameraActivity.SHOOT_CAMERA
                WatchCameraControlAnswer.Exit -> CameraActivity.EXIT_CAMERA
                WatchCameraControlAnswer.Unknown -> {
                    return
                }
            }
            data = Uri.parse("package:$packageName")
        }

        startActivity(intent)
    }

    override fun onWatchHeartAlarm() {
        // val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//        val emergencyNumber = "911" // TODO: check using TelephonyManager.isEmergencyNumber; .getEmergencyNumberList()
//        val intent = Intent(Intent.ACTION_CALL_EMERGENCY)
//        intent.data = Uri.parse("tel:$emergencyNumber")
//        startActivity(intent)
    }

    override fun onWatchFindMobilePhone() {
        // TODO take out of mute if necessary
        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val ringtone = RingtoneManager.getRingtone(applicationContext, ringtoneUri)
        ringtone.play()
    }

    override fun onWatchRegularReminder() {
        // TODO just send a notification
        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val ringtone = RingtoneManager.getRingtone(applicationContext, ringtoneUri)
        ringtone.play()
    }

    override fun onWatchSleepReminder() {
        // TODO just send a notification
        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone = RingtoneManager.getRingtone(applicationContext, ringtoneUri)
        ringtone.play()
    }

    override fun onResetSequenceNumbers() {
        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val key = AppSettings.getWatchKey(this, sharedPreferences)
        if (key != null) {
            // Note: If you say the exact right things, this will return status=0. Otherwise, it will never respond.
            //val userId = AppSettings.get???(this, sharedPreferences) // TODO get watch user id or something
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