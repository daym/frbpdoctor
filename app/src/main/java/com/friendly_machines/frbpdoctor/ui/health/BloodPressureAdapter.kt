package com.friendly_machines.frbpdoctor.ui.health

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.frbpdoctor.R

class BloodPressureAdapter(private val data: List<com.friendly_machines.fr_yhe_api.commondata.BpDataBlock>) : RecyclerView.Adapter<BloodPressureViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BloodPressureViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_blood_pressure, parent, false)
        return BloodPressureViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BloodPressureViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return data.size
    }
}