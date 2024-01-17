package com.friendly_machines.frbpdoctor.service

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.IBinder
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.friendly_machines.frbpdoctor.AppSettings
import com.friendly_machines.frbpdoctor.MyApplication
import com.friendly_machines.frbpdoctor.ui.settings.SettingsActivity
import com.friendly_machines.frbpdoctor.watchprotocol.command.WatchCommand
import com.friendly_machines.frbpdoctor.watchprotocol.bluetooth.WatchCommunicator
import com.friendly_machines.frbpdoctor.watchprotocol.bluetooth.WatchCommunicator.Companion.encodeWatchString
import com.friendly_machines.frbpdoctor.watchprotocol.bluetooth.WatchListener
import com.friendly_machines.frbpdoctor.watchprotocol.command.WatchBindCommand
import com.friendly_machines.frbpdoctor.watchprotocol.command.WatchDeviceInfoCommand
import com.friendly_machines.frbpdoctor.watchprotocol.command.WatchGetAlarmCommand
import com.friendly_machines.frbpdoctor.watchprotocol.command.WatchGetBatteryStateCommand
import com.friendly_machines.frbpdoctor.watchprotocol.command.WatchGetBpDataCommand
import com.friendly_machines.frbpdoctor.watchprotocol.command.WatchGetDeviceConfigCommand
import com.friendly_machines.frbpdoctor.watchprotocol.command.WatchGetHeatDataCommand
import com.friendly_machines.frbpdoctor.watchprotocol.command.WatchGetSleepDataCommand
import com.friendly_machines.frbpdoctor.watchprotocol.command.WatchGetSportDataCommand
import com.friendly_machines.frbpdoctor.watchprotocol.command.WatchGetStepDataCommand
import com.friendly_machines.frbpdoctor.watchprotocol.command.WatchGetWatchFaceCommand
import com.friendly_machines.frbpdoctor.watchprotocol.command.WatchSetAlarmCommand
import com.friendly_machines.frbpdoctor.watchprotocol.command.WatchSetMessageCommand
import com.friendly_machines.frbpdoctor.watchprotocol.command.WatchSetProfileCommand
import com.friendly_machines.frbpdoctor.watchprotocol.command.WatchSetTimeCommand
import com.friendly_machines.frbpdoctor.watchprotocol.command.WatchSetWeatherCommand
import com.friendly_machines.frbpdoctor.watchprotocol.command.WatchUnbindCommand
import com.friendly_machines.frbpdoctor.watchprotocol.notification.WatchRawResponse
import com.friendly_machines.frbpdoctor.watchprotocol.notification.WatchResponse
import com.friendly_machines.frbpdoctor.watchprotocol.notification.big.MessageType
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.Calendar
import kotlin.system.exitProcess

class WatchCommunicationService : Service(), WatchListener {
    companion object {
        const val TAG: String = "WatchCommunicationService"
    }

    //private var listeners = HashSet<WatchCommunicationServiceListener>()
    private val communicator = WatchCommunicator()

    private fun showSetMandatorySettingsDialog() {
        val settingsIntent = Intent(this, SettingsActivity::class.java)
        settingsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(settingsIntent)
    }

    private fun areMandatorySettingsSet(): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        return AppSettings.MANDATORY_SETTINGS.all { key ->
            sharedPreferences.contains(key)
        }
    }

    private fun setKeyDigest(keyDigest: ByteArray) {
        communicator.setKeyDigest(keyDigest)
    }

    // TODO: commandQueue.onComplete on connection loss??
    private val commandQueue = PublishSubject.create<WatchCommand>()

    private fun queueAll(
        command: WatchCommand,
    ) {
        commandQueue.onNext(command)
    }

    override fun onCreate() {
        communicator.addListener(this)
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            // TODO: if bluetooth adapter is disabled, enable it.
//            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
//            if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled) {
//                //val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//                //startActivityForResult(activity, enableBtIntent, REQUEST_ENABLE_BT)
//                bluetoothAdapter.enableWithProfile();
//            }

            if (!areMandatorySettingsSet()) {
                showSetMandatorySettingsDialog()
            } else {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

                val byteArrayString = sharedPreferences.getString(AppSettings.KEY_WATCH_KEY_DIGEST, null)
                if (byteArrayString != null) {
                    val keyDigest = Base64.decode(byteArrayString, Base64.DEFAULT)
                    this.setKeyDigest(keyDigest)
                    val watchMacAddress = sharedPreferences.getString(AppSettings.KEY_WATCH_MAC_ADDRESS, "")!!
                    val bleDevice = MyApplication.rxBleClient.getBleDevice(watchMacAddress)
                    communicator.start(bleDevice, commandQueue)
                } else {
                    // Maybe later
                    Log.e(TAG, "KeyDigest was null")
                    stopSelf()
                }
            }
        } else {
            Log.e(TAG, "No bluetooth LE support in the phone")
            stopSelf()
            // Quit entire app
            exitProcess(1)
        }
    }

    override fun onDestroy() {
        communicator.removeListener(this)
        communicator.onDestroy()
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
    }
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        Log.d(TAG, "Service started")
//        // onStartCommand can be called multiple times although the service is already running. Use this.connecting as a proxy for that.
//        return START_STICKY // TODO opportunistic
//    }


    inner class WatchCommunicationServiceBinder : Binder() {
        //        fun getService(): WatchCommunicationService {
//            return this@WatchCommunicationService
//        }
        fun setProfile(height: Byte, weight: Byte, sex: Byte, age: Byte) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@WatchCommunicationService)

            val keyDigestBase64 = sharedPreferences.getString(AppSettings.KEY_WATCH_KEY_DIGEST, null)
            if (keyDigestBase64 != null) {
                // TODO: This is a workaround to a dumb ordering bug, and in an ideal world it would be unnecessary
                setKeyDigest(
                    Base64.decode(
                        keyDigestBase64, Base64.DEFAULT
                    )
                )
            }

            queueAll(WatchSetProfileCommand(height, weight, sex, age))
        }

        fun setWeather(
            weatherType: Short, temp: Byte, maxTemp: Byte, minTemp: Byte, dummy: Byte/*0*/, month: Byte, dayOfMonth: Byte, dayOfWeekMondayBased: Byte, title: String
        ) = queueAll(WatchSetWeatherCommand(weatherType, temp, maxTemp, minTemp, dummy, month, dayOfMonth, dayOfWeekMondayBased, encodeWatchString(title)))

        fun setMessage(type: MessageType, time: Int, title: String, content: String) = queueAll(
            WatchSetMessageCommand(
                type.code, time, encodeWatchString(title), encodeWatchString(content)
            )
        )

        fun setTime() {
            val currentTimeInSeconds = System.currentTimeMillis() / 1000
            val instance: Calendar = Calendar.getInstance()
            val timezoneInSeconds = (instance.get(Calendar.DST_OFFSET) + instance.get(Calendar.ZONE_OFFSET)) / 1000
            queueAll(
                WatchSetTimeCommand(
                    currentTimeInSeconds.toInt(), timezoneInSeconds
                )
            )
        }

        fun getBatteryState() = queueAll(WatchGetBatteryStateCommand())
        fun getAlarm() = queueAll(WatchGetAlarmCommand())

        fun setAlarm(
            action: Byte, // 0
            id: Int, open: Byte, hour: Byte, min: Byte, title: Byte, repeats: ByteArray
        ) = queueAll(
            WatchSetAlarmCommand(action, id, open, hour, min, title, repeats),
        )

        fun bindWatch(userId: Long, key: ByteArray) = queueAll(
            WatchBindCommand(userId, key)
        )

        fun unbindWatch() = queueAll(WatchUnbindCommand())
        fun getDeviceConfig() = queueAll(WatchGetDeviceConfigCommand())
        fun getBpData() = queueAll(WatchGetBpDataCommand())
        fun getSleepData() = queueAll(WatchGetSleepDataCommand())
        fun getStepData() = queueAll(WatchGetStepDataCommand())
        fun getHeatData() = queueAll(WatchGetHeatDataCommand())
        fun getWatchFace() = queueAll(WatchGetWatchFaceCommand())
        fun getSportData() = queueAll(WatchGetSportDataCommand())

        fun addListener(that: WatchListener): WatchCommunicationService {
            return this@WatchCommunicationService.addListener(that)
        }

        fun removeListener(that: WatchListener) {
            return this@WatchCommunicationService.removeListener(that)
        }
    }

    private val binder = WatchCommunicationServiceBinder()

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        super.onUnbind(intent)
        return true // call onRebind
    }

    // TODO what if we are restarted: will the listener be restored?!

    fun removeListener(listener: WatchListener) {
        communicator.removeListener(listener)
    }

    private fun addListener(listener: WatchListener): WatchCommunicationService {
        communicator.addListener(listener)
        return this
    }

    override fun onWatchResponse(response: WatchResponse) {
        when (response) {
            is WatchResponse.DeviceInfo -> {
            }

            else -> {
            }
        }
    }

    override fun onMtuResponse(mtu: Int) {
        queueAll(
            WatchDeviceInfoCommand((mtu - 7).toShort())
        )
    }

    override fun onException(exception: Throwable) {
        super.onException(exception)
        Toast.makeText(this, "Error: $exception", Toast.LENGTH_LONG).show()
    }
}

