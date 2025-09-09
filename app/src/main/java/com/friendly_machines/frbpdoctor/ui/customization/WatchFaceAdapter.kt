package com.friendly_machines.frbpdoctor.ui.customization

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.friendly_machines.frbpdoctor.R

class WatchFaceAdapter(
    private val data: List<com.friendly_machines.fr_yhe_api.commondata.WatchDialDataBlock>,
    private val onSelectClick: (id: Int) -> Unit,
    private val onDeleteClick: (id: Int) -> Unit
) : RecyclerView.Adapter<WatchFaceAdapter.WatchFaceViewHolder>() {

    private var selectedItemId: Int? = null

    inner class WatchFaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val idTextView: TextView = itemView.findViewById(R.id.idTextView)
        private val blockNumberTextView: TextView = itemView.findViewById(R.id.blockNumberTextView)
        private val canDeleteCheckbox: CheckBox = itemView.findViewById(R.id.canDeleteCheckBox)
        private val versionTextView: TextView = itemView.findViewById(R.id.versionTextView)
        private val selectButton: MaterialButton = itemView.findViewById(R.id.selectButton)
        private val deleteButton: MaterialButton = itemView.findViewById(R.id.deleteButton)

//        fun getSelectedIndex(): Int? {
//            val position = absoluteAdapterPosition
//            if (position != RecyclerView.NO_POSITION) {
//                return position
//            }
//            return null
//        }

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = data[position]
//                    val previousSelectedItem = selectedItemPosition
//                    selectedItemPosition = position
//                    notifyItemChanged(previousSelectedItem)
//                    notifyItemChanged(selectedItemPosition)
                    setSelectedItemId(item.id)
                    notifyDataSetChanged()
                }
            }
        }

        fun bind(item: com.friendly_machines.fr_yhe_api.commondata.WatchDialDataBlock) {
            idTextView.text = "Watch Face ${item.id}"
            blockNumberTextView.text = item.blockNumber.toString()
            canDeleteCheckbox.isChecked = item.canDelete
            versionTextView.text = item.version.toString()
            
            selectButton.setOnClickListener {
                onSelectClick(item.id)
            }
            
            deleteButton.setOnClickListener {
                onDeleteClick(item.id)
            }
            
            // Enable/disable delete button based on canDelete flag
            deleteButton.isEnabled = item.canDelete
            
            if (item.id == selectedItemId) {
                itemView.setBackgroundResource(R.drawable.selected_item_background)
                itemView.isSelected = true
            } else {
                itemView.setBackgroundResource(R.drawable.selected_item_background)
                itemView.isSelected = false
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchFaceViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_watch_dial, parent, false)
        return WatchFaceViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: WatchFaceViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun getSelectedItemId(): Int? {
        return selectedItemId
    }
    fun setSelectedItemId(id: Int?) {
        selectedItemId = id
    }
}