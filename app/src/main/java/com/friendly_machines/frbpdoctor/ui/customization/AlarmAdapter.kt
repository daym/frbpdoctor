package com.friendly_machines.frbpdoctor.ui.customization

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.frbpdoctor.R

class AlarmAdapter(
    private val data: List<com.friendly_machines.fr_yhe_api.commondata.AlarmDataBlock>,
    private val onDeleteClick: (id: Byte) -> Unit
) : RecyclerView.Adapter<AlarmViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarm, parent, false)
        return AlarmViewHolder(itemView, onDeleteClick)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return data.size
    }
}