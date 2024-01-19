package com.friendly_machines.frbpdoctor.ui.health

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.watchprotocol.notification.big.AlarmDataBlock
import com.friendly_machines.frbpdoctor.watchprotocol.notification.big.BpDataBlock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val idTextView: TextView = itemView.findViewById(R.id.idTextView)
    private val openTextView: TextView = itemView.findViewById(R.id.openTextView)
    private val hourTextView: TextView = itemView.findViewById(R.id.hourTextView)
    private val minTextView: TextView = itemView.findViewById(R.id.minTextView)
    private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
    private val repeatMondayTextView: TextView = itemView.findViewById(R.id.repeatMondayTextView)
    private val repeatTuesdayTextView: TextView = itemView.findViewById(R.id.repeatTuesdayTextView)
    private val repeatWednesdayTextView: TextView = itemView.findViewById(R.id.repeatWednesdayTextView)
    private val repeatThursdayTextView: TextView = itemView.findViewById(R.id.repeatThursdayTextView)
    private val repeatFridayTextView: TextView = itemView.findViewById(R.id.repeatFridayTextView)
    private val repeatSaturdayTextView: TextView = itemView.findViewById(R.id.repeatSaturdayTextView)
    private val repeatSundayTextView: TextView = itemView.findViewById(R.id.repeatSundayTextView)

    fun bind(item: AlarmDataBlock) {
        idTextView.text = item.id.toString()
        openTextView.text = item.open.toString()
        hourTextView.text = item.hour.toString()
        minTextView.text = item.min.toString()
        titleTextView.text = item.title.toString()
        repeatMondayTextView.text = item.repeats[0].toString()
        repeatTuesdayTextView.text = item.repeats[1].toString()
        repeatWednesdayTextView.text = item.repeats[2].toString()
        repeatThursdayTextView.text = item.repeats[3].toString()
        repeatFridayTextView.text = item.repeats[4].toString()
        repeatSaturdayTextView.text = item.repeats[5].toString()
        repeatSundayTextView.text = item.repeats[6].toString()
    }
}
