package com.friendly_machines.frbpdoctor.ui.health

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.fr_yhe_api.commondata.HBloodOxygenDataBlock
import com.friendly_machines.frbpdoctor.R
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class BloodOxygenAdapter(private val bloodOxygenData: List<HBloodOxygenDataBlock>) : RecyclerView.Adapter<BloodOxygenAdapter.BloodOxygenViewHolder>() {

    class BloodOxygenViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timestampText: TextView = itemView.findViewById(R.id.timestampText)
        val bloodOxygenText: TextView = itemView.findViewById(R.id.bloodOxygenText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BloodOxygenViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_blood_oxygen, parent, false)
        return BloodOxygenViewHolder(view)
    }

    override fun onBindViewHolder(holder: BloodOxygenViewHolder, position: Int) {
        val oxygenData = bloodOxygenData[position]
        
        // Format timestamp using locale-dependent format
        val formatter = DateTimeFormatter.ofLocalizedDateTime(
            FormatStyle.SHORT,
            FormatStyle.SHORT
        ).withZone(ZoneId.systemDefault())
        val instant = oxygenData.startTime.toInstant()
        holder.timestampText.text = formatter.format(instant)
        
        // Format blood oxygen level
        holder.bloodOxygenText.text = "${oxygenData.bloodOxygen}%"
    }

    override fun getItemCount(): Int = bloodOxygenData.size
}