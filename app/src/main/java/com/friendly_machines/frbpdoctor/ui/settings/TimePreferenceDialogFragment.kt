package com.friendly_machines.frbpdoctor.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TimePicker
import androidx.preference.PreferenceDialogFragmentCompat
import java.text.SimpleDateFormat
import java.util.Calendar

class TimePreferenceDialogFragment : PreferenceDialogFragmentCompat() {
    private var hour = 0
    private var minute = 0
    private var timePicker: TimePicker? = null
    private fun getHour(dateString: String?): Int {
        val datePieces = dateString!!.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return datePieces[0].toInt()
    }

    private fun getMinute(dateString: String?): Int {
        val datePieces = dateString!!.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return datePieces[1].toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var timeString = timePreference.text
        if (timeString.isNullOrEmpty()) {
            val calendar = Calendar.getInstance()
            val df = SimpleDateFormat("HH:mm")
            timeString = df.format(calendar.time)
        }
        hour = getHour(timeString)
        minute = getMinute(timeString)
    }

    override fun onCreateDialogView(context: Context): View? {
        timePicker = TimePicker(getContext())
        return timePicker
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        timePicker!!.hour = hour
        timePicker!!.minute = minute
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            hour = timePicker!!.hour
            minute = timePicker!!.minute
            val dateString = String.format("%d:%d", hour, minute)
            val preference = timePreference
            if (preference.callChangeListener(dateString)) {
                preference.text = dateString
            }
        }
    }

    private val timePreference: TimePreference
        get() = preference as TimePreference

    companion object {
        fun newInstance(key: String?): TimePreferenceDialogFragment {
            val fragment = TimePreferenceDialogFragment()
            val b = Bundle(1)
            b.putString(ARG_KEY, key)
            fragment.arguments = b
            return fragment
        }
    }
}