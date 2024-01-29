package com.friendly_machines.frbpdoctor.ui.health

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.fr_yhe_med.command.WatchChangeAlarmAction
import com.friendly_machines.fr_yhe_api.commondata.AlarmTitleMed
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar

class EditAlarmDialog(private val action: com.friendly_machines.fr_yhe_med.command.WatchChangeAlarmAction) : DialogFragment() {
    private lateinit var timeEditText: EditText
    private lateinit var dayCheckBoxes: List<CheckBox>

    // Interface to communicate the selected alarm data to the calling activity/fragment
    interface OnAlarmSetListener {
        fun onAlarmSet(enabled: Boolean, title: com.friendly_machines.fr_yhe_api.commondata.AlarmTitleMed, hour: Byte, min: Byte, repeatOnDaysOfWeek: BooleanArray)
    }

    private var listener: OnAlarmSetListener? = null
    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar[Calendar.HOUR_OF_DAY]
        val minute = calendar[Calendar.MINUTE]
        val timePickerDialog = TimePickerDialog(
            requireContext(), { _, hourOfDay: Int, minuteOfDay: Int ->
                val selectedTime = String.format("%02d:%02d", hourOfDay, minuteOfDay)
                timeEditText.setText(selectedTime)
            }, hour, minute, DateFormat.is24HourFormat(requireContext())
        )
        timePickerDialog.show()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater
        val view: View = inflater.inflate(R.layout.dialog_edit_alarm, null)
        timeEditText = view.findViewById(R.id.timeEditText)!!
        dayCheckBoxes = arrayOf(
            R.id.repeatMondayCheckBox, R.id.repeatTuesdayCheckBox, R.id.repeatWednesdayCheckBox, R.id.repeatThursdayCheckbox, R.id.repeatFridayCheckbox, R.id.repeatSaturdayCheckBox, R.id.repeatSundayCheckbox
        ).map {
            view.findViewById(it)!!
        }
        timeEditText.setOnClickListener {
            showTimePickerDialog()
        }

        val timeEditText = timeEditText
        val titleSpinner = view.findViewById<Spinner>(R.id.titleSpinner)
        val enabledCheckBox = view.findViewById<CheckBox>(R.id.enabledCheckBox)

        // Populate the title spinner with enum values
        val titleAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            com.friendly_machines.fr_yhe_api.commondata.AlarmTitleMed.values()
        )
        titleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        titleSpinner.adapter = titleAdapter

        builder.setPositiveButton(when (action) {
            com.friendly_machines.fr_yhe_med.command.WatchChangeAlarmAction.Add -> "Add Alarm"
            com.friendly_machines.fr_yhe_med.command.WatchChangeAlarmAction.Edit -> "Change Alarm"
        }) { _, _ ->
            val timeString = timeEditText.text.toString()
            val repeatOnDaysOfWeek = BooleanArray(7)
            for (i in 0..6) {
                repeatOnDaysOfWeek[i] = dayCheckBoxes[i].isChecked
            }
            val title = titleSpinner.selectedItem as com.friendly_machines.fr_yhe_api.commondata.AlarmTitleMed
            if (timeString.isNotEmpty()) {
                try {
                    val dateFormat = SimpleDateFormat("HH:mm")
                    val parsedDate = dateFormat.parse(timeString)
                    val calendar = Calendar.getInstance()
                    calendar.time = parsedDate
                    val hours = calendar[Calendar.HOUR_OF_DAY].toByte()
                    val minutes = calendar[Calendar.MINUTE].toByte()
//                    val hours: Int = parsedDate.hours
//                    val minutes: Int = parsedDate.minutes
                    listener?.onAlarmSet(enabledCheckBox.isChecked, title, hours, minutes, repeatOnDaysOfWeek)
                } catch (e: ParseException) {
                    Toast.makeText(requireContext(), "Invalid time", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Set up negative button to cancel
        builder.setNegativeButton("Cancel") { _, _ -> dismiss() }
        builder.setView(view)
        return builder.create()
    }

    fun addListener(listener: OnAlarmSetListener) {
        assert(this.listener == null)
        this.listener = listener
    }
}