package com.friendly_machines.frbpdoctor.ui.health

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import com.friendly_machines.frbpdoctor.R

class AlarmDetailFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        _binding = FragmentHomeBinding.inflate(inflater, container, false)
//        val root: View = binding.root

        // TODO use TimePickerDialog

        val view = inflater.inflate(R.layout.fragment_alarm_detail, container, false)
        //super.onCreateView(name, context, attrs)
        val alarmTimePicker = view.findViewById<TimePicker>(R.id.alarmTimePicker)
//        val alarmTimeButton = binding.alarmTimeButton
//        addAlarmTimeButton.setOnClickListener {
//            //alarmTimePicker.visibility = View.VISIBLE
//        }
        alarmTimePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            val selectedTime = "$hourOfDay:$minute"
            // TODO: Store selectedTime

            alarmTimePicker.visibility = View.GONE
        }
        return view
    }
}