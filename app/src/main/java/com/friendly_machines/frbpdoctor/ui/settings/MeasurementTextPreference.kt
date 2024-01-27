package com.friendly_machines.frbpdoctor.ui.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.EditTextPreference
import com.friendly_machines.frbpdoctor.R

class MeasurementTextPreference(context: Context, attrs: AttributeSet) : EditTextPreference(context, attrs) {
    private var unit: String? = null

    fun getUnit(): String? {
        return unit
    }

    override fun getPersistedString(defaultReturnValue: String?): String {
        return getPersistedInt(0).toString()
    }

    override fun persistString(value: String?): Boolean {
        val intValue = try{
            Integer.parseInt(value!!)
        }
        catch (e: NumberFormatException){
            e.printStackTrace()
            0
        }
        return persistInt(intValue)
    }
    class SimpleSummaryProvider private constructor() : SummaryProvider<MeasurementTextPreference?> {
        override fun provideSummary(preference: MeasurementTextPreference): CharSequence? {
            return if (preference.unit == null) {
                preference.text
            } else {
                val text = preference.text
                val unit = preference.unit
                "$text $unit"
            }
        }

        companion object {
            val instance: SimpleSummaryProvider by lazy {
                SimpleSummaryProvider()
            }
        }
    }

    init {
        // Retrieve the unit attribute from XML
        val typedArray = context.obtainStyledAttributes(attrs,
            R.styleable.MeasurementTextPreference
        )
        unit = typedArray.getString(R.styleable.MeasurementTextPreference_unit)
        typedArray.recycle()

        summaryProvider = SimpleSummaryProvider.instance
    }
}
