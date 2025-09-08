package com.friendly_machines.frbpdoctor.ui.health

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.fr_yhe_api.commondata.HHistoryHeartRateDataBlock
import com.friendly_machines.frbpdoctor.R
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class HeartRateAdapter(private val heartRateData: List<HHistoryHeartRateDataBlock>) : RecyclerView.Adapter<HeartRateAdapter.HeartRateViewHolder>() {

    class HeartRateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timestampText: TextView = itemView.findViewById(R.id.timestampText)
        val heartRateText: TextView = itemView.findViewById(R.id.heartRateText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeartRateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_heart_rate, parent, false)
        return HeartRateViewHolder(view)
    }

    override fun onBindViewHolder(holder: HeartRateViewHolder, position: Int) {
        val heartData = heartRateData[position]
        
        // Format timestamp using locale-dependent format
        val formatter = DateTimeFormatter.ofLocalizedDateTime(
            FormatStyle.SHORT,
            FormatStyle.SHORT
        ).withZone(ZoneId.systemDefault())
        val instant = heartData.timestamp.toInstant()
        holder.timestampText.text = formatter.format(instant)
        
        // Format heart rate
        holder.heartRateText.text = "${heartData.heartRateInBpm} bpm"
    }

    override fun getItemCount(): Int = heartRateData.size
}