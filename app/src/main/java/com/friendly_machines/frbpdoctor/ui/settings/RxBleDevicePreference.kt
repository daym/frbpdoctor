package com.friendly_machines.frbpdoctor.ui.settings

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.DialogPreference
import com.friendly_machines.frbpdoctor.R
import com.polidea.rxandroidble3.RxBleDevice

/**
 * A dialog preference that shown calendar in the dialog.
 *
 *
 * Saves a string value.
 */
class RxBleDevicePreference(context: Context?, attrs: AttributeSet?) : DialogPreference(context!!, attrs) {
    /**
     * Gets the date as a string from the current data storage.
     *
     * @return string representation of the date.
     */
    var device: RxBleDevice? = null

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? {
        return a.getString(index)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        //setDate(getPersistedString((String) defaultValue));
        device = defaultValue as RxBleDevice?
    }

    /**
     * Saves the date as a string in the current data storage.
     *
     * @param device string representation of the date to save.
     */
    fun setDevice2(device: RxBleDevice) {
        val wasBlocking = shouldDisableDependents()
        this.device = device
        persistString(device.macAddress)
        val isBlocking = shouldDisableDependents()
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking)
        }
        notifyChanged()
    }

    /**
     * A simple [SummaryProvider] implementation for an
     * [RxBleDevicePreference]. If no value has been set, the summary displayed will be 'Not
     * set', otherwise the summary displayed will be the value set for this preference.
     */
    class SimpleSummaryProvider private constructor() : SummaryProvider<RxBleDevicePreference?> {
        override fun provideSummary(preference: RxBleDevicePreference): CharSequence? {
            return if (preference.device == null) {
                preference.context.getString(R.string.not_set)
            } else {
                preference.device!!.macAddress
            }
        }

        companion object {
            private var simpleSummaryProvider: SimpleSummaryProvider? = null

            /**
             * Retrieve a singleton instance of this simple
             * [SummaryProvider] implementation.
             *
             * @return a singleton instance of this simple
             * [SummaryProvider] implementation
             */
            val instance: SimpleSummaryProvider?
                get() {
                    if (simpleSummaryProvider == null) {
                        simpleSummaryProvider = SimpleSummaryProvider()
                    }
                    return simpleSummaryProvider
                }
        }
    }
}