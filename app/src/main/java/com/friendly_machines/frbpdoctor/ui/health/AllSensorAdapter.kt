package com.friendly_machines.frbpdoctor.ui.health

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.fr_yhe_api.commondata.HHistoryAllDataBlock
import com.friendly_machines.frbpdoctor.R
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class AllSensorAdapter(private val allSensorData: List<HHistoryAllDataBlock>) : RecyclerView.Adapter<AllSensorAdapter.AllSensorViewHolder>() {

    class AllSensorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timestampText: TextView = itemView.findViewById(R.id.timestampText)
        val heartRateText: TextView = itemView.findViewById(R.id.heartRateText)
        val bloodPressureText: TextView = itemView.findViewById(R.id.bloodPressureText)
        val temperatureText: TextView = itemView.findViewById(R.id.temperatureText)
        val oxygenText: TextView = itemView.findViewById(R.id.oxygenText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllSensorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_all_sensor, parent, false)
        return AllSensorViewHolder(view)
    }

    override fun onBindViewHolder(holder: AllSensorViewHolder, position: Int) {
        val sensorData = allSensorData[position]
        
        // Format timestamp using locale-dependent format
        val formatter = DateTimeFormatter.ofLocalizedDateTime(
            java.time.format.FormatStyle.SHORT,
            java.time.format.FormatStyle.SHORT
        ).withZone(ZoneId.systemDefault())
        val instant = sensorData.startTime.toInstant()
        holder.timestampText.text = formatter.format(instant)
        
        // Format sensor readings
        holder.heartRateText.text = "HR: ${sensorData.heartRate} BPM"
        holder.bloodPressureText.text = "BP: ${sensorData.systolicBloodPressure}/${sensorData.diastolicBloodPressure}"
        holder.temperatureText.text = "Temp: ${sensorData.tempInt} float ${sensorData.tempFloat} °C"
        holder.oxygenText.text = "SpO₂: ${sensorData.bloodOxygen}%"
    }

    override fun getItemCount(): Int = allSensorData.size
}