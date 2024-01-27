package com.friendly_machines.frbpdoctor.ui.settings

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.DialogPreference
import com.friendly_machines.frbpdoctor.R
import com.polidea.rxandroidble3.RxBleDevice

class RxBleDevicePreference(context: Context?, attrs: AttributeSet?) : DialogPreference(context!!, attrs) {
    var device: RxBleDevice? = null

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? {
        return a.getString(index)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        //setDate(getPersistedString((String) defaultValue));
        device = defaultValue as RxBleDevice?
    }

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