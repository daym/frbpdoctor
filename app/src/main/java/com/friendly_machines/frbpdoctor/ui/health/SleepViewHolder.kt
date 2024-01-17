package com.friendly_machines.frbpdoctor.ui.health

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.service.big.SleepDataBlock
import com.friendly_machines.frbpdoctor.service.big.StepsDataBlock

class SleepViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val startTimestampTextView: TextView = itemView.findViewById(R.id.startTimestampTextView)
    private val endTimestampTextView: TextView = itemView.findViewById(R.id.endTimestampTextView)
    private val flagTextView: TextView = itemView.findViewById(R.id.flagTextView)

    fun bind(item: SleepDataBlock) {
        startTimestampTextView.text = "${item.startTimestamp}"
        endTimestampTextView.text = "${item.endTimestamp}"
        flagTextView.text = item.flag.toString()
    }
}
