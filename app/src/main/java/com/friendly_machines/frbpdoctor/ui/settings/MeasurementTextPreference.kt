package com.friendly_machines.frbpdoctor.ui.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.EditTextPreference
import com.friendly_machines.frbpdoctor.R

class MeasurementTextPreference(context: Context, attrs: AttributeSet) : EditTextPreference(context, attrs) {
    private var unit: String? = null

    init {
        // Retrieve the unit attribute from XML
        val typedArray = context.obtainStyledAttributes(attrs,
            R.styleable.MeasurementTextPreference
        )
        unit = typedArray.getString(R.styleable.MeasurementTextPreference_unit)
        typedArray.recycle()

        // Set the unit as the summary for display
        summary = unit
    }

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
}
