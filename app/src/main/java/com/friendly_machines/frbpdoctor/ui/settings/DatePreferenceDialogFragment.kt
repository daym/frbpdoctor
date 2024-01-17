package com.friendly_machines.frbpdoctor.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import androidx.preference.PreferenceDialogFragmentCompat
import java.text.SimpleDateFormat
import java.util.Calendar

class DatePreferenceDialogFragment : PreferenceDialogFragmentCompat() {
    private var year = 0
    private var month = 0
    private var day = 0
    private var datePicker: DatePicker? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var dateValue = datePreference.date
        if (dateValue.isNullOrEmpty()) {
            val calendar = Calendar.getInstance()
            val df = SimpleDateFormat("yyyy-MM-dd")
            dateValue = df.format(calendar.time)
        }
        year = getYear(dateValue)
        month = getMonth(dateValue)
        day = getDay(dateValue)
    }

    override fun onCreateDialogView(context: Context): View? {
        datePicker = DatePicker(getContext())
        // Show spinner dialog for old APIs.
        datePicker!!.calendarViewShown = false
        return datePicker
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        datePicker!!.updateDate(year, month - 1, day)
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            year = datePicker!!.year
            month = datePicker!!.month + 1
            day = datePicker!!.dayOfMonth
            val dateVal = (year.toString() + "-"
                    + month.toString() + "-"
                    + day.toString())
            val preference = datePreference
            if (preference.callChangeListener(dateVal)) {
                preference.date = dateVal
            }
        }
    }

    private val datePreference: DatePreference
        private get() = preference as DatePreference

    private fun getYear(dateString: String?): Int {
        val datePieces = dateString!!.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return datePieces[0].toInt()
    }

    private fun getMonth(dateString: String?): Int {
        val datePieces = dateString!!.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return datePieces[1].toInt()
    }

    private fun getDay(dateString: String?): Int {
        val datePieces = dateString!!.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return datePieces[2].toInt()
    }

    companion object {
        fun newInstance(key: String?): DatePreferenceDialogFragment {
            val fragment = DatePreferenceDialogFragment()
            val b = Bundle(1)
            b.putString(ARG_KEY, key)
            fragment.arguments = b
            return fragment
        }
    }
}