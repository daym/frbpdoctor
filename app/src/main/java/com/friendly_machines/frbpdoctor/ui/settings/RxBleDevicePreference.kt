package com.friendly_machines.frbpdoctor.ui.settings

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.widget.Toast
import androidx.preference.DialogPreference
import androidx.preference.EditTextPreference
import com.friendly_machines.frbpdoctor.R
import com.polidea.rxandroidble3.RxBleDevice

class RxBleDevicePreference(context: Context, attrs: AttributeSet?) : EditTextPreference(context, attrs) {

//    class SimpleSummaryProvider private constructor() : SummaryProvider<RxBleDevicePreference?> {
//        override fun provideSummary(preference: RxBleDevicePreference): CharSequence? {
//            return if (preference.device == null) {
//                preference.context.getString(R.string.not_set)
//            } else {
//                preference.device!!.macAddress
//            }
//        }
//
//        companion object {
//            val instance: SimpleSummaryProvider by lazy {
//                SimpleSummaryProvider()
//            }
//        }
//    }
//
//    init {
//        summaryProvider = SimpleSummaryProvider.instance
//    }
}