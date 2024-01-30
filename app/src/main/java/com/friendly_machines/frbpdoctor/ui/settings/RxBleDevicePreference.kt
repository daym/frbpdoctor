package com.friendly_machines.frbpdoctor.ui.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.EditTextPreference
import com.friendly_machines.frbpdoctor.MyApplication

class RxBleDevicePreference(context: Context, attrs: AttributeSet?) : EditTextPreference(context, attrs) {

    class SimpleSummaryProvider private constructor() : SummaryProvider<RxBleDevicePreference?> {
        override fun provideSummary(preference: RxBleDevicePreference): CharSequence? {
            val watchMacAddress = preference.text
            if (watchMacAddress != null) {
                val bleDevice = MyApplication.rxBleClient.getBleDevice(watchMacAddress)
                return bleDevice.name
            } else {
                return null
            }
        }

        companion object {
            val instance: SimpleSummaryProvider by lazy {
                SimpleSummaryProvider()
            }
        }
    }

    init {
        summaryProvider = SimpleSummaryProvider.instance
    }
}