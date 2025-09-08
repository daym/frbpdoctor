package com.friendly_machines.frbpdoctor.ui.health

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.fr_yhe_api.commondata.HHistorySportModeDataBlock
import com.friendly_machines.frbpdoctor.R
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class SportModeAdapter(private val sportModeData: List<HHistorySportModeDataBlock>) : RecyclerView.Adapter<SportModeAdapter.SportModeViewHolder>() {

    class SportModeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timestampText: TextView = itemView.findViewById(R.id.timestampText)
        val sportModeText: TextView = itemView.findViewById(R.id.sportModeText)
        val durationText: TextView = itemView.findViewById(R.id.durationText)
        val caloriesText: TextView = itemView.findViewById(R.id.caloriesText)
        val distanceText: TextView = itemView.findViewById(R.id.distanceText)
        val stepsText: TextView = itemView.findViewById(R.id.stepsText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SportModeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sport_mode, parent, false)
        return SportModeViewHolder(view)
    }

    override fun onBindViewHolder(holder: SportModeViewHolder, position: Int) {
        val sportData = sportModeData[position]
        
        // Format timestamp using locale-dependent format
        val formatter = DateTimeFormatter.ofLocalizedDateTime(
            FormatStyle.SHORT,
            FormatStyle.SHORT
        ).withZone(ZoneId.systemDefault())
        val instant = sportData.startTime.toInstant()
        holder.timestampText.text = formatter.format(instant)
        
        // Format sport mode name
        val sportModes = arrayOf("Walking", "Running", "Cycling", "Swimming", "Climbing", "Basketball", "Football", "Badminton")
        val sportMode = if (sportData.mode < sportModes.size) sportModes[sportData.mode.toInt()] else "Sport ${sportData.mode}"
        holder.sportModeText.text = sportMode
        
        // Format duration (convert seconds to mm:ss)
        val minutes = sportData.sportTime / 60
        val seconds = sportData.sportTime % 60
        holder.durationText.text = "Duration: ${String.format("%d:%02d", minutes, seconds)}"
        
        // Format calories
        holder.caloriesText.text = "Cal: ${sportData.calories}"
        
        // Format distance (convert to km with 1 decimal place)
        val distanceKm = sportData.distance / 1000.0
        holder.distanceText.text = "Distance: ${String.format("%.1f km", distanceKm)}"
        
        // Format steps
        holder.stepsText.text = "Steps: ${sportData.steps}"
    }

    override fun getItemCount(): Int = sportModeData.size
}