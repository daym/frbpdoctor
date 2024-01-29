package com.friendly_machines.frbpdoctor.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponseType
import com.friendly_machines.frbpdoctor.AppSettings
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.WatchCommunicationClientShorthand
import com.polidea.rxandroidble3.scan.ScanResult
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// FIXME Android has an actual "Settings Fragment" in the menu

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener, ScannerFragment.ScannerResultListener {
    companion object {
        const val TAG: String = "SettingsFragment"
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

    private fun setProfile(profile: AppSettings.Profile) {
        val age = calculateYearsSinceDate(profile.birthdayString)
        if (age <= 0 || age >= 256)
            throw RuntimeException("profile age is invalid")
        WatchCommunicationClientShorthand.bindExecOneCommandUnbind(requireContext(), WatchResponseType.SetProfile) {
            it.setProfile(profile.height, profile.weight, profile.sex, age.toByte())
        }
    }

    private fun clearAllPreferences() {
        unbindWatch()

        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        AppSettings.clear(sharedPreferences)


        Toast.makeText(requireContext(), "Settings cleared", Toast.LENGTH_LONG).show()
        // Since the settings gui doesn't update, close it so the user doesn't see the wrong values.
        activity?.finish()
        System.exit(1) // ...
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        findPreference<Preference>("clear_preferences")?.setOnPreferenceClickListener {
            clearAllPreferences()
            true
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        when (preference) {
            is RxBleDevicePreference -> {
                val scannerFragment = ScannerFragment(this)
                scannerFragment.show(requireActivity().supportFragmentManager, "ScannerFragment")
            }

            is DatePreference -> {
                val f: DialogFragment
                f = DatePreferenceDialogFragment.newInstance(preference.getKey())
                f.setTargetFragment(this, 0) // TODO
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

    override fun onScanningUserSelectedDevice(scanResult: ScanResult) {
        val device = scanResult.bleDevice
        findPreference<RxBleDevicePreference>("watchMacAddress")?.text = device.macAddress

        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        var watchCommunicatorClassname = "unknown"
        val key = if (com.friendly_machines.fr_yhe_pro.bluetooth.WatchCommunicator.compatibleWith(scanResult.scanRecord)) {
            watchCommunicatorClassname = "com.friendly_machines.fr_yhe_pro.bluetooth.WatchCommunicator"
            "dummy".toByteArray(Charsets.US_ASCII)
        } else if (com.friendly_machines.fr_yhe_med.bluetooth.WatchCommunicator.compatibleWith(scanResult.scanRecord)) {
            watchCommunicatorClassname = "com.friendly_machines.fr_yhe_med.bluetooth.WatchCommunicator"
            scanResult.scanRecord.manufacturerSpecificData[2257].copyOfRange(0, 16)
        } else {
            "unknown".toByteArray(Charsets.US_ASCII)
        }
        AppSettings.setWatchCommunicatorSettings(requireContext(), sharedPreferences, key, watchCommunicatorClassname)

        // Note: It's possible that scanning doesn't find anything when we are already connected.
        AppSettings.getUserId(sharedPreferences)?.let { userId ->
            // TODO: If userId is null, synth one from the digits in device.name or something (and store it in SharedPreferences and also in Settings GUI)
            bindWatch(userId, key)
        }
    }
}
