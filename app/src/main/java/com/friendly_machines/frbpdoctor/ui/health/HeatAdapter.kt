package com.friendly_machines.frbpdoctor.ui.health

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.fr_yhe_api.commondata.HeatDataBlock

class HeatAdapter(private val data: List<com.friendly_machines.fr_yhe_api.commondata.HeatDataBlock>) : RecyclerView.Adapter<HeatViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeatViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_heat, parent, false)
        return HeatViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HeatViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return data.size
    }
}