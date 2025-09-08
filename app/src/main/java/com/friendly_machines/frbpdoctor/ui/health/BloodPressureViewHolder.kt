package com.friendly_machines.frbpdoctor.ui.health

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.frbpdoctor.R
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class BloodPressureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
    private val systolicPressureTextView: TextView = itemView.findViewById(R.id.systolicPressureTextView)
    private val diastolicPressureTextView: TextView = itemView.findViewById(R.id.diastolicPressureTextView)
    private val pulseTextView: TextView = itemView.findViewById(R.id.pulseTextView)

    fun bind(item: com.friendly_machines.fr_yhe_api.commondata.HBloodDataBlock) {
        val instant = item.bloodStartTime.toInstant()
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        timestampTextView.text = localDateTime.format(formatter)
        systolicPressureTextView.text = item.bloodSystolicPressure.toInt().toString()
        diastolicPressureTextView.text = item.bloodDiastolicPressure.toInt().toString()
        pulseTextView.text = "N/A" // HBloodDataBlock doesn't have pulse data
    }
}
