package com.friendly_machines.frbpdoctor.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceManager
import com.friendly_machines.frbpdoctor.AppSettings
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.WatchCommunicationClientShorthand
import com.friendly_machines.frbpdoctor.watchprotocol.notification.WatchResponse
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
        WatchCommunicationClientShorthand.bindExecOneCommandUnbind(requireContext(), WatchResponse.Unbind(0)) {
            it.unbindWatch()
        }
    }

    private fun bindWatch(userId: Long, key: ByteArray) {
        WatchCommunicationClientShorthand.bindExecOneCommandUnbind(requireContext(), WatchResponse.Bind(0)) {
            // Just in case the watch was bound somewhere else, unbind it. Better not.
            //it.unbindWatch()
            it.bindWatch(userId, key)
        }
    }

    private fun setProfile(profile: AppSettings.Profile) {
        val age = calculateYearsSinceDate(profile.birthdayString)
        assert(age < 256)
        assert(age > 0)
        WatchCommunicationClientShorthand.bindExecOneCommandUnbind(requireContext(), WatchResponse.SetProfile(0)) {
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
                    setProfile(profile)
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
        findPreference<RxBleDevicePreference>("watchMacAddress")?.setDevice2(device)

        val key = scanResult.scanRecord.manufacturerSpecificData[2257].copyOfRange(0, 16)
        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        AppSettings.setWatchKey(requireContext(), sharedPreferences, key)

        // Note: It's possible that scanning doesn't find anything when we are already connected.
        AppSettings.getUserId(sharedPreferences)?.let { userId ->
            // TODO: If userId is null, synth one from the digits in device.name or something (and store it in SharedPreferences and also in Settings GUI)
            bindWatch(userId, key)
        }
    }
}
