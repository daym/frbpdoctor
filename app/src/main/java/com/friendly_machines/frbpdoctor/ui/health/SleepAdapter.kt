package com.friendly_machines.frbpdoctor.ui.health

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.frbpdoctor.R

class SleepAdapter(private val data: List<com.friendly_machines.fr_yhe_api.commondata.SleepDataBlock>) : RecyclerView.Adapter<SleepViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SleepViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_steps, parent, false)
        return SleepViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SleepViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return data.size
    }
}