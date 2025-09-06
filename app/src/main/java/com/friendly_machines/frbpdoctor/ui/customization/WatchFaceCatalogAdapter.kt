package com.friendly_machines.frbpdoctor.ui.customization

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.frbpdoctor.R

class WatchFaceCatalogAdapter(
    private val items: List<WatchfaceCatalogItem>,
    private val onItemClick: (WatchfaceCatalogItem) -> Unit
) : RecyclerView.Adapter<WatchFaceCatalogAdapter.ViewHolder>() {

    private var selectedPosition = -1

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val previewImage: ImageView = itemView.findViewById(R.id.watchfacePreviewImage)
        private val nameText: TextView = itemView.findViewById(R.id.watchfaceNameText) 
        private val idText: TextView = itemView.findViewById(R.id.watchfaceIdText)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val previousSelected = selectedPosition
                    selectedPosition = position
                    notifyItemChanged(previousSelected)
                    notifyItemChanged(selectedPosition)
                    onItemClick(items[position])
                }
            }
        }

        fun bind(item: WatchfaceCatalogItem, isSelected: Boolean) {
            nameText.text = item.name
            idText.text = "ID: ${item.dialplateId}"
            
            // Load preview image
            val preview = WatchFaceCatalogLoader.loadWatchfacePreview(itemView.context, item)
            if (preview != null) {
                previewImage.setImageBitmap(preview)
            } else {
                previewImage.setImageResource(R.drawable.ic_launcher_background)
            }
            
            // Highlight selected item
            itemView.setBackgroundColor(if (isSelected) Color.LTGRAY else Color.TRANSPARENT)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_watchface_catalog, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position == selectedPosition)
    }

    override fun getItemCount() = items.size
    
    fun getSelectedItem(): WatchfaceCatalogItem? {
        return if (selectedPosition >= 0) items[selectedPosition] else null
    }
}