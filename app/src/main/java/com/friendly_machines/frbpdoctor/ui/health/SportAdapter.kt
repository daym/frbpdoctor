package com.friendly_machines.frbpdoctor.ui.health

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.service.big.SportDataBlock

class SportAdapter(private val data: List<SportDataBlock>) : RecyclerView.Adapter<SportViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SportViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sport, parent, false)
        return SportViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SportViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return data.size
    }
}