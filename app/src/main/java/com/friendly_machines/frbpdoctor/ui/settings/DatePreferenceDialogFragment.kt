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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var dateString = datePreference.text
        if (dateString.isNullOrEmpty()) {
            val calendar = Calendar.getInstance()
            val df = SimpleDateFormat("yyyy-MM-dd")
            dateString = df.format(calendar.time)
        }
        year = getYear(dateString)
        month = getMonth(dateString)
        day = getDay(dateString)
    }

    override fun onCreateDialogView(context: Context): View? {
        datePicker = DatePicker(getContext())
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
            val dateString = String.format("%d-%02d-%02d", year, month, day);
            val preference = datePreference
            if (preference.callChangeListener(dateString)) {
                preference.text = dateString
            }
        }
    }

    private val datePreference: DatePreference
        get() = preference as DatePreference

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