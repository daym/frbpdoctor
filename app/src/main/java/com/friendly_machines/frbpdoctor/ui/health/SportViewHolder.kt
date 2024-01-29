package com.friendly_machines.frbpdoctor.ui.health

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.fr_yhe_api.commondata.SportDataBlock

class SportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val startTimestampTextView: TextView = itemView.findViewById(R.id.startTimestampTextView)
    private val sportTypeTextView: TextView = itemView.findViewById(R.id.sportTypeTextView)
    private val avgHeartRateTextView: TextView = itemView.findViewById(R.id.avgHeartRateTextView)
    private val heatTextView: TextView = itemView.findViewById(R.id.heatTextView)
    private val runningDistanceTextView: TextView = itemView.findViewById(R.id.runningDistanceTextView)
    private val durationTextView: TextView = itemView.findViewById(R.id.durationTextView)
    private val speedTextView: TextView = itemView.findViewById(R.id.speedTextView)
    private val stepCountTextView: TextView = itemView.findViewById(R.id.stepCountTextView)

    fun bind(item: com.friendly_machines.fr_yhe_api.commondata.SportDataBlock) {
        startTimestampTextView.text = "${item.timestamp}"
        sportTypeTextView.text = item.sportType.toString()
        avgHeartRateTextView.text = item.avgHeartRate.toString()
        heatTextView.text = item.heat.toString()
        runningDistanceTextView.text = item.runningDistance.toString()
        durationTextView.text = item.duration.toString()
        speedTextView.text = item.speed.toString()
        stepCountTextView.text = item.stepCount.toString()
    }
}
