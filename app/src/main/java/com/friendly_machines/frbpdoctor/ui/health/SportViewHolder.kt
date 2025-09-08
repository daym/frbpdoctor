package com.friendly_machines.frbpdoctor.ui.health

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.frbpdoctor.R
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.ZoneId

class SportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val startTimestampTextView: TextView = itemView.findViewById(R.id.startTimestampTextView)
    private val sportTypeTextView: TextView = itemView.findViewById(R.id.sportTypeTextView)
    private val avgHeartRateTextView: TextView = itemView.findViewById(R.id.avgHeartRateTextView)
    private val heatTextView: TextView = itemView.findViewById(R.id.heatTextView)
    private val runningDistanceTextView: TextView = itemView.findViewById(R.id.runningDistanceTextView)
    private val durationTextView: TextView = itemView.findViewById(R.id.durationTextView)
    private val speedTextView: TextView = itemView.findViewById(R.id.speedTextView)
    private val stepCountTextView: TextView = itemView.findViewById(R.id.stepCountTextView)

    fun bind(item: com.friendly_machines.fr_yhe_api.commondata.HSportDataBlock) {
        // Format timestamp using modern time API
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT)
            .withZone(ZoneId.systemDefault())
        startTimestampTextView.text = formatter.format(item.startTime.toInstant())
        
        sportTypeTextView.text = "Sport" // FIXME: map sport type
        avgHeartRateTextView.text = "N/A" // Not available in HSportDataBlock
        heatTextView.text = item.calories.toString()
        runningDistanceTextView.text = item.distance.toString()
        durationTextView.text = "${(item.endTime.toInstant().epochSecond - item.startTime.toInstant().epochSecond) / 60} min"
        speedTextView.text = "N/A" // Calculate from distance/duration if needed
        stepCountTextView.text = item.steps.toString()
    }
}
