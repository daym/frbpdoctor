package com.friendly_machines.frbpdoctor.ui.health

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.frbpdoctor.R

class StepsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val dayTimestampTextView: TextView = itemView.findViewById(R.id.dayTimestampTextView)
    private val currentStepsTextView: TextView = itemView.findViewById(R.id.currentStepsTextView)
    private val targetStepsTextView: TextView = itemView.findViewById(R.id.targetStepsTextView)

    fun bind(item: com.friendly_machines.fr_yhe_api.commondata.StepsDataBlock) {
        dayTimestampTextView.text = "Date: ${item.dayTimestamp}"
        currentStepsTextView.text = item.currentSteps.toString()
        targetStepsTextView.text = item.targetSteps.toString()
    }
}
