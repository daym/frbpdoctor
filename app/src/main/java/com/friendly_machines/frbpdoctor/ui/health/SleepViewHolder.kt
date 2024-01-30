package com.friendly_machines.frbpdoctor.ui.health

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.frbpdoctor.R

class SleepViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val startTimestampTextView: TextView = itemView.findViewById(R.id.startTimestampTextView)
    private val endTimestampTextView: TextView = itemView.findViewById(R.id.endTimestampTextView)
    private val flagTextView: TextView = itemView.findViewById(R.id.flagTextView)

    fun bind(item: com.friendly_machines.fr_yhe_api.commondata.SleepDataBlock) {
        startTimestampTextView.text = "${item.startTimestamp}"
        endTimestampTextView.text = "${item.endTimestamp}"
        flagTextView.text = item.quality.toString()
    }
}
