package com.friendly_machines.frbpdoctor.ui.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.frbpdoctor.databinding.FragmentScannerBinding
import com.polidea.rxandroidble3.scan.ScanResult

/**
 * [RecyclerView.Adapter]
 */
class MyScannerRecyclerViewAdapter(
    private val values: List<ScanResult>,
    private val onItemClickListener: OnItemClickListener

) : RecyclerView.Adapter<MyScannerRecyclerViewAdapter.ViewHolder>() {
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentScannerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.contentView.text = item.toString()
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentScannerBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        val contentView: TextView = binding.content

        init {
            itemView.setOnClickListener(this)
        }

//        fun bind(device: RxBleDevice) {
//            val deviceName = device.name ?: "Unknown Device"
//            deviceNameTextView.text = deviceName
//        }

        // TODO fun bind...
        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }

        override fun onClick(view: View) {
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onItemClickListener.onItemClick(position)
            }
        }

    }

}