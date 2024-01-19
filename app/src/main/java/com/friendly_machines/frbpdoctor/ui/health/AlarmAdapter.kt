package com.friendly_machines.frbpdoctor.ui.health

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.watchprotocol.notification.big.AlarmDataBlock

class AlarmAdapter(private val data: List<AlarmDataBlock>) : RecyclerView.Adapter<AlarmViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarm, parent, false)
        return AlarmViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return data.size
    }
}