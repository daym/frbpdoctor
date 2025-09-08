package com.friendly_machines.frbpdoctor.ui.health

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.frbpdoctor.R
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.ZoneId

class SleepViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val startTimestampTextView: TextView = itemView.findViewById(R.id.startTimestampTextView)
    private val endTimestampTextView: TextView = itemView.findViewById(R.id.endTimestampTextView)
    private val flagTextView: TextView = itemView.findViewById(R.id.flagTextView)

    fun bind(item: com.friendly_machines.fr_yhe_api.commondata.HSleepDataBlock) {
        // Format timestamps using modern time API
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT)
            .withZone(ZoneId.systemDefault())
        startTimestampTextView.text = formatter.format(item.startTime.toInstant())
        endTimestampTextView.text = formatter.format(item.endTime.toInstant())
        
        // Calculate sleep quality from deep sleep ratio
        val quality = if (item.deepSleepCount > 0) {
            (item.deepSleepTotal.toFloat() / (item.deepSleepTotal + item.lightSleepTotal) * 100).toInt()
        } else 0
        flagTextView.text = "${quality}%"
    }
}
