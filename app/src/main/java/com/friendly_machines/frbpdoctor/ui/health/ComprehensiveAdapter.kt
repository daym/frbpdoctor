package com.friendly_machines.frbpdoctor.ui.health

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.fr_yhe_api.commondata.HHistoryComprehensiveMeasurementDataBlock
import com.friendly_machines.frbpdoctor.R
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ComprehensiveAdapter(private val comprehensiveData: List<HHistoryComprehensiveMeasurementDataBlock>) : RecyclerView.Adapter<ComprehensiveAdapter.ComprehensiveViewHolder>() {

    class ComprehensiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timestampText: TextView = itemView.findViewById(R.id.timestampText)
        val heartRateText: TextView = itemView.findViewById(R.id.heartRateText)
        val bloodPressureText: TextView = itemView.findViewById(R.id.bloodPressureText)
        val temperatureText: TextView = itemView.findViewById(R.id.temperatureText)
        val oxygenText: TextView = itemView.findViewById(R.id.oxygenText)
        val fatigueText: TextView = itemView.findViewById(R.id.fatigueText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComprehensiveViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comprehensive, parent, false)
        return ComprehensiveViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComprehensiveViewHolder, position: Int) {
        val compData = comprehensiveData[position]
        
        // Format timestamp using locale-dependent format
        val formatter = DateTimeFormatter.ofLocalizedDateTime(
            FormatStyle.SHORT,
            FormatStyle.SHORT
        ).withZone(ZoneId.systemDefault())
        val instant = compData.timestamp.toInstant()
        holder.timestampText.text = formatter.format(instant)
        
        // Format comprehensive measurements
        holder.heartRateText.text = "Blood Sugar: ${compData.bloodSugarInteger}.${compData.bloodSugarFloat}"
        holder.bloodPressureText.text = "Uric Acid: ${compData.uricAcid}"
        holder.temperatureText.text = "Blood Ketone: ${compData.bloodKetoneInteger}.${compData.bloodKetoneFloat}"
        holder.oxygenText.text = "Cholesterol: ${compData.cholesterolInteger}.${compData.cholesterolFloat}"
        holder.fatigueText.text = "Triglyceride: ${compData.triglycerideCholesterolInteger}.${compData.triglycerideCholesterolFloat}"
    }

    override fun getItemCount(): Int = comprehensiveData.size
}