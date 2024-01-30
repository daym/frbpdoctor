package com.friendly_machines.frbpdoctor.ui.health

import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.frbpdoctor.R

class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val idTextView: TextView = itemView.findViewById(R.id.idTextView)
    private val enabledCheckBox: CheckBox = itemView.findViewById(R.id.enabledCheckBox)
    private val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
    private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
    private val repeatMondayCheckBox: CheckBox = itemView.findViewById(R.id.repeatMondayCheckBox)
    private val repeatTuesdayCheckBox: CheckBox = itemView.findViewById(R.id.repeatTuesdayCheckBox)
    private val repeatWednesdayCheckBox: CheckBox = itemView.findViewById(R.id.repeatWednesdayCheckBox)
    private val repeatThursdayCheckBox: CheckBox = itemView.findViewById(R.id.repeatThursdayCheckBox)
    private val repeatFridayCheckBox: CheckBox = itemView.findViewById(R.id.repeatFridayCheckBox)
    private val repeatSaturdayCheckBox: CheckBox = itemView.findViewById(R.id.repeatSaturdayCheckBox)
    private val repeatSundayCheckBox: CheckBox = itemView.findViewById(R.id.repeatSundayCheckBox)

    fun bind(item: com.friendly_machines.fr_yhe_api.commondata.AlarmDataBlock) {
        idTextView.text = item.id.toString()
        enabledCheckBox.isChecked = item.enabled != 0.toByte()
        timeTextView.text = "${item.hour}:${item.min}"
        titleTextView.text = com.friendly_machines.fr_yhe_api.commondata.AlarmTitleMed.parse(item.title).toString()
        repeatMondayCheckBox.isChecked = item.repeats[0] != 0.toByte()
        repeatTuesdayCheckBox.isChecked = item.repeats[1] != 0.toByte()
        repeatWednesdayCheckBox.isChecked = item.repeats[2] != 0.toByte()
        repeatThursdayCheckBox.isChecked = item.repeats[3] != 0.toByte()
        repeatFridayCheckBox.isChecked = item.repeats[4] != 0.toByte()
        repeatSaturdayCheckBox.isChecked = item.repeats[5] != 0.toByte()
        repeatSundayCheckBox.isChecked = item.repeats[6] != 0.toByte()
    }
}
