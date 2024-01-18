package com.friendly_machines.frbpdoctor.ui.settings

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.Bundle
import android.os.IBinder
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceManager
import com.friendly_machines.frbpdoctor.AppSettings
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.service.WatchCommunicationService
import com.friendly_machines.frbpdoctor.ui.home.MainActivity
import com.polidea.rxandroidble3.scan.ScanResult
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// FIXME Android has an actual "Settings Fragment" in the menu

class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener,
    ScannerFragment.ScannerResultListener {
    companion object {
        const val TAG: String = "SettingsFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun clearAllPreferences() {
        val sharedPreferences: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(requireContext())

        AppSettings.clear(sharedPreferences)

        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as WatchCommunicationService.WatchCommunicationServiceBinder
                binder.unbindWatch() // no reaction
                //watchCommunicationService.setListener(bluetoothServiceListener) to check whether it worked
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                // Handle service disconnection

            }
        }

        val serviceIntent = Intent(requireContext(), WatchCommunicationService::class.java)
        requireActivity().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

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
        initSummary(getPreferenceScreen())

        val clearPreferencesPreference = findPreference<Preference>("clear_preferences")
        clearPreferencesPreference?.setOnPreferenceClickListener {
            clearAllPreferences()
            // TODO message here
            true
        }

        val watchMacAddressPreference = findPreference<Preference>("watchMacAddress")
        watchMacAddressPreference?.setOnPreferenceClickListener {
            // unbind watch

            val serviceConnection = object : ServiceConnection {
                //private var disconnector: WatchCommunicationService? = null
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val binder = service as WatchCommunicationService.WatchCommunicationServiceBinder
                    binder.unbindWatch()
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                }
            }
            val context = this.requireContext()
            val serviceIntent = Intent(context, WatchCommunicationService::class.java)
            if (!context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)) {
                Log.e(MainActivity.TAG, "Could not bind to WatchCommunicationService")
            }
            true
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is RxBleDevicePreference) {
            val scannerFragment = ScannerFragment(this)
            scannerFragment.show(requireActivity().supportFragmentManager, "ScannerFragment")
        } else if (preference is DatePreference) {
            val f: DialogFragment
            f = DatePreferenceDialogFragment.newInstance(preference.getKey())
            f.setTargetFragment(this, 0) // TODO
            f.show(parentFragmentManager, null)
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
        if (preference.key == "watchMacAddress") {
            val sharedPreferences: SharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(requireContext())
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

            val yearsDifference =
                currentCalendar.get(Calendar.YEAR) - selectedCalendar.get(Calendar.YEAR)

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
                // TODO wind down the amount of stuff per second
                val serviceConnection = object : ServiceConnection {
                    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                        val binder =
                            service as WatchCommunicationService.WatchCommunicationServiceBinder

                        val weight = AppSettings.getWeight(sharedPreferences)
                        val height = AppSettings.getHeight(sharedPreferences)
                        AppSettings.getBirthday(sharedPreferences)?.let {
                            val birthday = it
                            AppSettings.getSex(sharedPreferences)?.let {
                                val sex = it
                                val age = calculateYearsSinceDate(birthday)
                                assert(age < 256)
                                assert(age > 0)
                                if (weight > 0 && height > 0 && sex > 0) {
                                    binder.setProfile(
                                        height,
                                        weight,
                                        sex,
                                        age.toByte()
                                    )
                                }
                            }
                        }
                    }

                    override fun onServiceDisconnected(name: ComponentName?) {
                        // TODO
                    }
                }

                val serviceIntent = Intent(requireContext(), WatchCommunicationService::class.java)
                requireActivity().bindService(
                    serviceIntent,
                    serviceConnection,
                    Context.BIND_AUTO_CREATE
                )
                //!!

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
        val watchMacAddressPreference =
            findPreference<Preference>("watchMacAddress") as RxBleDevicePreference
        watchMacAddressPreference.setDevice2(device)

        val data = scanResult.scanRecord.manufacturerSpecificData
        val k = data.keyAt(0) // 2257
        val key = data.valueAt(0).copyOfRange(0, 16)
        val keyDigest = MessageDigest.getInstance("MD5").digest(key)
        val sharedPreferences: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        // Assumption: We never want the user to be able to edit keydigest.
        AppSettings.setKeyDigest(sharedPreferences, keyDigest)
        // Assumption: unbind() was already done before scanning. Note: It's possible that scanning doesn't find anything when we are already connected.
        // Should be Long, but Android is weird.
        AppSettings.getUserId(sharedPreferences)?.let {
            val userId = it
            val serviceConnection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val binder = service as WatchCommunicationService.WatchCommunicationServiceBinder
                    // Race condition: This will be reached before the service is completely up (i.e. before connection is set)! Then it will try to do connection!!writeCharacteristic and that will fail.
                    binder.bindWatch(userId, key)

                    // TODO maybe binder.getDeviceConfig() but after response or something

                    // FIXME wait until the command was processed.
                    val activity = requireActivity()
                    activity.unbindService(this)
                }
                override fun onServiceDisconnected(name: ComponentName?) {
                }
            }

            // FIXME: We changed who we connect to--so restart the service so that it reconnects.

            // useless requireContext().stopService(serviceIntent)
            // useless requireContext().startService(serviceIntent)
            val activity = requireActivity()
            // activity.unbindService(serviceConnection) // TODO does this block?
            val serviceIntent = Intent(requireContext(), WatchCommunicationService::class.java)
            if (!activity.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)) {
                Log.e(TAG, "Could not bind to WatchCommunicationService")
            }
        }
    }

    override fun onStop() {
        super.onStop()
    }
}
