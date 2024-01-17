package com.friendly_machines.frbpdoctor.ui.health

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.service.big.HeatDataBlock
import com.friendly_machines.frbpdoctor.service.big.StepsDataBlock

class HeatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val dayTimestampTextView: TextView = itemView.findViewById(R.id.dayTimestampTextView)
    private val baseTextView: TextView = itemView.findViewById(R.id.baseTextView)
    private val walkTextView: TextView = itemView.findViewById(R.id.walkTextView)
    private val sportTextView: TextView = itemView.findViewById(R.id.sportTextView)

    fun bind(item: HeatDataBlock) {
        dayTimestampTextView.text = "Date: ${item.dayTimestamp}"
        baseTextView.text = item.base.toString()
        walkTextView.text = item.walk.toString()
        sportTextView.text = item.sport.toString()
    }
}
