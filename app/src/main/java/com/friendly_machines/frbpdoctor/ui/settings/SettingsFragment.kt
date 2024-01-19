package com.friendly_machines.frbpdoctor.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceManager
import com.friendly_machines.frbpdoctor.AppSettings
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.WatchCommunicationServiceClientShorthand
import com.friendly_machines.frbpdoctor.watchprotocol.notification.WatchResponse
import com.polidea.rxandroidble3.scan.ScanResult
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// FIXME Android has an actual "Settings Fragment" in the menu

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener, ScannerFragment.ScannerResultListener {
    companion object {
        const val TAG: String = "SettingsFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun unbindWatch() {
        WatchCommunicationServiceClientShorthand.bindExecOneCommandUnbind(requireContext(), WatchResponse.Unbind(0)) {
            it.unbindWatch()
        }
    }

    private fun bindWatch(userId: Long, key: ByteArray) {
        WatchCommunicationServiceClientShorthand.bindExecOneCommandUnbind(requireContext(), WatchResponse.Bind(0)) {
            it.unbindWatch()
            it.bindWatch(userId, key)
        }
    }

    private fun setProfile(profile: AppSettings.Profile) {
        val age = calculateYearsSinceDate(profile.birthdayString)
        assert(age < 256)
        assert(age > 0)
        WatchCommunicationServiceClientShorthand.bindExecOneCommandUnbind(requireContext(), WatchResponse.SetProfile(0)) {
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

        // FIXME test
        //        for (preferenceKey in AppSettings.MANDATORY_SETTINGS) {
//            val preference = findPreference<Preference>(preferenceKey)
//            preference?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, _ ->
//                onSharedPreferenceChanged(preferenceManager.sharedPreferences, preferenceKey)
//                true
//            }
//        }
        initSummary(preferenceScreen)

        val clearPreferencesPreference = findPreference<Preference>("clear_preferences")
        clearPreferencesPreference?.setOnPreferenceClickListener {
            clearAllPreferences()
            // TODO message here
            true
        }

        val watchMacAddressPreference = findPreference<Preference>("watchMacAddress")
        watchMacAddressPreference?.setOnPreferenceClickListener {
            unbindWatch()
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
        if (preference.key == "watchMacAddress") {
            val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val macAddress = AppSettings.getMacAddress(sharedPreferences)
            preference.summary = macAddress
        }
    }

    /**
     * Sets up summary providers for the preferences.
     *
     * @param p The preference to set up summary provider.
     */
    private fun setPreferenceSummary(p: Preference) {
        // No need to set up preference summaries for checkbox preferences because
        // they can be set up in xml using summaryOff and summary On
        if (p is DatePreference) {
            p.setSummaryProvider(DatePreference.SimpleSummaryProvider.instance)
        } else if (p is EditTextPreference) {
            p.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
        }
    }

    /**
     * Walks through all preferences.
     *
     * @param p The starting preference to search from.
     */
    private fun initSummary(p: Preference) {
        if (p is PreferenceGroup) {
            val pGrp: PreferenceGroup = p
            for (i in 0 until pGrp.preferenceCount) {
                initSummary(pGrp.getPreference(i))
            }
        } else {
            setPreferenceSummary(p)
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
//            if (key == "watchMacAddress") {
//                val macAddress = sharedPreferences.getString(AppSettings.KEY_WATCH_MAC_ADDRESS, "")
//                val watchMacAddressPreference = findPreference<Preference>("watchMacAddress")
//                watchMacAddressPreference?.summary = macAddress
//            }
            if (key == AppSettings.KEY_USER_HEIGHT || key == AppSettings.KEY_USER_WEIGHT || key == AppSettings.KEY_USER_SEX || key == AppSettings.KEY_USER_BIRTHDAY) {
                AppSettings.getProfileSettings(sharedPreferences)?.let { profile ->
                    // TODO wind down the amount of stuff per second
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
        val watchMacAddressPreference = findPreference<Preference>("watchMacAddress") as RxBleDevicePreference
        watchMacAddressPreference.setDevice2(device)

        val data = scanResult.scanRecord.manufacturerSpecificData
        val k = data.keyAt(0) // 2257
        val key = data.valueAt(0).copyOfRange(0, 16)
        val keyDigest = MessageDigest.getInstance("MD5").digest(key)
        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        // Assumption: We never want the user to be able to edit keyDigest.
        AppSettings.setKeyDigest(sharedPreferences, keyDigest)
        // Note: It's possible that scanning doesn't find anything when we are already connected.
        AppSettings.getUserId(sharedPreferences)?.let { userId ->
            // TODO: If userId is null, synth one from the digits in device.name or something (and store it in SharedPreferences and also in Settings GUI)
            bindWatch(userId, key)
        }
    }

    override fun onStop() {
        super.onStop()
    }
}
