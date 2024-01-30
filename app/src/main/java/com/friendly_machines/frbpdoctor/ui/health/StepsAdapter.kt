package com.friendly_machines.frbpdoctor.ui.health

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.frbpdoctor.R

class StepsAdapter(private val data: List<com.friendly_machines.fr_yhe_api.commondata.StepsDataBlock>) : RecyclerView.Adapter<StepsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepsViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_steps, parent, false)
        return StepsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: StepsViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return data.size
    }
}