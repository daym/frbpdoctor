package com.friendly_machines.frbpdoctor.ui.settings

import android.app.Activity
import android.companion.AssociationInfo
import android.companion.AssociationRequest
import android.companion.CompanionDeviceManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.friendly_machines.fr_yhe_api.commondata.SkinColor
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchDriver
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponseType
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchTimePosition
import com.friendly_machines.fr_yhe_api.commondata.WatchWearingArm
import com.friendly_machines.frbpdoctor.AppSettings
import com.friendly_machines.frbpdoctor.MyApplication
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.WatchCommunicationClientShorthand
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.Executor
import kotlin.system.exitProcess


// FIXME Android has an actual "Settings Fragment" in the menu

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        const val TAG: String = "SettingsFragment"
        const val SELECT_DEVICE_REQUEST_CODE = 42
        val watchDrivers: List<IWatchDriver> = listOf(
            com.friendly_machines.fr_yhe_pro.bluetooth.WatchDriver(),
            com.friendly_machines.fr_yhe_med.bluetooth.WatchDriver(),
        )
    }

    private fun unbindWatch() {
        WatchCommunicationClientShorthand.bindExecOneCommandUnbind(requireContext(), WatchResponseType.Unbind) {
            it.unbindWatch()
        }
    }

    private fun bindWatch(userId: Long, key: ByteArray) {
        WatchCommunicationClientShorthand.bindExecOneCommandUnbind(requireContext(), WatchResponseType.Bind) {
            // Just in case the watch was bound somewhere else, unbind it. Better not.
            //it.unbindWatch()
            it.bindWatch(userId, key)
        }
    }

    private fun setTime() {
        WatchCommunicationClientShorthand.bindExecOneCommandUnbind(requireContext(), WatchResponseType.SetProfile) {
            it.setTime()
        }
    }

    private fun setProfile(profile: AppSettings.Profile) {
        val age = calculateYearsSinceDate(profile.birthdayString)
        if (age <= 0 || age >= 256) throw RuntimeException("profile age is invalid")
        WatchCommunicationClientShorthand.bindExecOneCommandUnbind(requireContext(), WatchResponseType.SetProfile) {
            it.setProfile(profile.height, profile.weight, profile.sex, age.toByte(), profile.arm)
        }
    }

    private fun clearAllPreferences() {
        unbindWatch()

        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        AppSettings.clear(sharedPreferences)


        Toast.makeText(requireContext(), "Settings cleared", Toast.LENGTH_LONG).show()
        // Since the settings gui doesn't update, close it so the user doesn't see the wrong values.
        activity?.finish()
        exitProcess(1) // ...
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        findPreference<Preference>("clear_preferences")?.setOnPreferenceClickListener {
            clearAllPreferences()
            true
        }
        findPreference<Preference>("set_time_on_watch")?.setOnPreferenceClickListener {
            setTime()
            true
        }
    }

    private fun selectDevice(scanResult: android.bluetooth.le.ScanResult) {
        val watchMacAddress = scanResult.device.address
        val bleDevice = MyApplication.rxBleClient.getBleDevice(watchMacAddress)
        findPreference<RxBleDevicePreference>("watchMacAddress")?.text = watchMacAddress

        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        var watchCommunicatorClassname = "unknown"
        AppSettings.setWatchCommunicatorSettings(requireContext(), sharedPreferences, "unknown".toByteArray(Charsets.US_ASCII), watchCommunicatorClassname)
        watchDrivers.find {
            it.isCompatibleWith(scanResult.scanRecord)
        }?.let { watchDriver ->
            if (watchDriver.isCompatibleWith(scanResult.scanRecord)) {
                watchDriver.createCommunicator(scanResult.scanRecord) { key ->
                    watchCommunicatorClassname = watchDriver.id
                    AppSettings.setWatchCommunicatorSettings(requireContext(), sharedPreferences, key, watchCommunicatorClassname)

                    // Note: It's possible that scanning doesn't find anything when we are already connected.
                    AppSettings.getUserId(sharedPreferences)?.let { userId ->
                        // TODO: If userId is null, synth one from the digits in device.name or something (and store it in SharedPreferences and also in Settings GUI)
                        bindWatch(userId, key)
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            SELECT_DEVICE_REQUEST_CODE -> when (resultCode) {
                Activity.RESULT_OK -> {
                    // The user chose to pair the app with a Bluetooth LE device.
                    val scanResult = data?.getParcelableExtra(android.companion.CompanionDeviceManager.EXTRA_DEVICE, android.bluetooth.le.ScanResult::class.java)
                    // data.getParcelableExtra(CompanionDeviceManager.EXTRA_ASSOCIATION); associationInfo.getAssociatedDevice missing in android 33
                    scanResult?.let {
                        selectDevice(it)
                    }
                }
            }

            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun scan() {
        val deviceManager = requireContext().getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager
        val watchChoosingExecutor: Executor = Executor { it.run() }
        val associationRequest = watchDrivers.fold(AssociationRequest.Builder().setDeviceProfile(AssociationRequest.DEVICE_PROFILE_WATCH)) { builder, watchDriver ->
            builder.addDeviceFilter(watchDriver.deviceFilter)
        }.build()
        deviceManager.associate(associationRequest, watchChoosingExecutor, object : CompanionDeviceManager.Callback() {
            override fun onAssociationPending(intentSender: IntentSender) {
                // Called when a device is found. Launch the IntentSender so the user can select the device they want to pair with.
                if (intentSender != null) {
                    // Doesn't work
//                        val launcher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
//                          onActivityResult(SELECT_DEVICE_REQUEST_CODE, result.resultCode, result.data)
//                        }
//                        launcher.launch(IntentSenderRequest.Builder(intentSender).build())
                    // The selection dialog is intentSender. Start it.
                    startIntentSenderForResult(intentSender, SELECT_DEVICE_REQUEST_CODE, null, 0, 0, 0, null)
                }
            }

            override fun onAssociationCreated(associationInfo: AssociationInfo) {
                // associationInfo.getAssociatedDevice missing in android 33
                associationInfo.deviceMacAddress
            }

            override fun onFailure(errorMessage: CharSequence?) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        when (preference) {
            is RxBleDevicePreference -> scan()
            is DatePreference -> {
                val f: DialogFragment
                f = DatePreferenceDialogFragment.newInstance(preference.getKey())
                f.setTargetFragment(this, 0) // TODO
                f.show(parentFragmentManager, null)
            }

            is TimePreference -> {
                val f: DialogFragment
                f = TimePreferenceDialogFragment.newInstance(preference.getKey())
                f.setTargetFragment(this, 0) // TODO
                f.show(parentFragmentManager, null)
            }

            is ColorPreference -> {
                val color = preference.text?.let { Color.parseColor(it) }
                ColorPickerDialogBuilder
                    .with(context)
                    .setTitle("Choose color for time display")
                    .initialColor(color ?: 0xFFFFFF)
                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                    .density(12)
                    .setOnColorSelectedListener { selectedColor ->
                        preference.text = "#" + Integer.toHexString(selectedColor and 0x00FFFFFF)
                    }
                    .setPositiveButton("ok") { dialog, selectedColor, allColors -> }
                    .setNegativeButton("cancel") { dialog, which -> }
                    .build()
                    .show()
            }

            is DeviceGInfoPreference -> {
                val f = DeviceGInfoFragment.newInstance()
                f.show(parentFragmentManager, null)
            }

            is DeviceFilesPreference -> {
                val f = DeviceFilesFragment.newInstance()
                f.show(parentFragmentManager, null)
            }

            else -> {
                super.onDisplayPreferenceDialog(preference)
            }
        }
    }

    private fun calculateYearsSinceDate(dateString: String): Int {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val selectedDate = dateFormat.parse(dateString)

            // Calculate the difference in years between the selected date and the current date
            val currentCalendar = Calendar.getInstance()
            val selectedCalendar = Calendar.getInstance().apply {
                time = selectedDate
            }

            val yearsDifference = currentCalendar.get(Calendar.YEAR) - selectedCalendar.get(Calendar.YEAR)

            // Check if the birthday has occurred this year
            if (currentCalendar.get(Calendar.DAY_OF_YEAR) < selectedCalendar.get(Calendar.DAY_OF_YEAR)) {
                return yearsDifference - 1
            }
            return yearsDifference
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key != null && sharedPreferences != null) {
            if (AppSettings.isProfileSetting(key)) {
                AppSettings.getProfileSettings(sharedPreferences)?.let { profile ->
                    try {
                        setProfile(profile)
                    } catch (e: RuntimeException) {
                        Toast.makeText(requireContext(), "Error setting profile: $e", Toast.LENGTH_LONG).show()
                    }
                }
            } else if (AppSettings.isDndSetting(key)) {
                val startTime = AppSettings.getDndStartTime(sharedPreferences)
                val endTime = AppSettings.getDndEndTime(sharedPreferences)
                WatchCommunicationClientShorthand.bindExecOneCommandUnbind(requireContext(), WatchResponseType.SetDndSettings) {
                    if (startTime != null && endTime != null) {
                        it.setDndSettings(1/*FIXME*/, startTime.hour, startTime.minute, endTime.hour, endTime.minute)
                    } else {
                        it.setDndSettings(0/*FIXME*/, 0, 0, 0, 0)
                    }
                }
            } else if (AppSettings.isUserWatchWearingArmSetting(key)) {
                val wearingArm = AppSettings.getUserWatchWearingArm(sharedPreferences)
                WatchCommunicationClientShorthand.bindExecOneCommandUnbind(requireContext(), WatchResponseType.SetWatchWearingArm) {
                    it.setWatchWearingArm(wearingArm ?: WatchWearingArm.Left)
                }
            } else if (AppSettings.isUserSkinColorSetting(key)) {
                val skinColor = AppSettings.getUserSkinColor(sharedPreferences)
                WatchCommunicationClientShorthand.bindExecOneCommandUnbind(requireContext(), WatchResponseType.SetSkinColor) {
                    it.setUserSkinColor(skinColor ?: SkinColor.Yellow)
                }
            } else if (AppSettings.isUserSleepSetting(key)) {
                val sleep = AppSettings.getUserSleep(sharedPreferences)
                WatchCommunicationClientShorthand.bindExecOneCommandUnbind(requireContext(), WatchResponseType.SetSkinColor) {
                    if (sleep != null) {
                        it.setUserSleep(sleep.hour, sleep.minute, sleep.repeats)
                    } else {
                        it.setUserSleep(0, 0, 0.toUByte())
                    }
                }
            } else if (AppSettings.isWatchScheduleEnabledSetting(key)) {
                val enabled = AppSettings.isWatchScheduleEnabled(sharedPreferences)
                WatchCommunicationClientShorthand.bindExecOneCommandUnbind(requireContext(), WatchResponseType.SetWatchScheduleEnabled) {
                    it.setScheduleEnabled(enabled)
                }
            } else if (AppSettings.isWatchTimeLayout(key)) {
                val timePosition = AppSettings.getWatchTimePosition(sharedPreferences)
                val rgb565Color = AppSettings.getWatchTimeColor(sharedPreferences)
                try {
                    WatchCommunicationClientShorthand.bindExecOneCommandUnbind(requireContext(), WatchResponseType.SetWatchTimeLayout) {
                        it.setWatchTimeLayout(timePosition ?: WatchTimePosition.Middle, rgb565Color ?: 0xFFFF.toUShort())
                    }
                } catch (e: RuntimeException) {
                    Toast.makeText(requireContext(), "Error setting profile: $e", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

}
