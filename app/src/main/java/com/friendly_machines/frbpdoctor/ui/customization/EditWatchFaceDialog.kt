package com.friendly_machines.frbpdoctor.ui.customization

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.fr_yhe_api.commondata.WatchChangeWatchDialAction
import com.friendly_machines.frbpdoctor.R

class EditWatchFaceDialog(private val action: WatchChangeWatchDialAction) : DialogFragment() {

    // Interface to communicate the selected watchface data to the calling activity/fragment
    interface OnWatchDialSetListener {
        fun onWatchDialSet(watchface: WatchfaceCatalogItem)
    }

    private var listener: OnWatchDialSetListener? = null
    private var adapter: WatchFaceCatalogAdapter? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater
        val view: View = inflater.inflate(R.layout.dialog_edit_watch_dial, null)

        // Load watchface catalog
        val catalog = WatchFaceCatalogLoader.loadCatalog(requireContext())
        
        // Set up RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.watchfaceCatalogRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        
        adapter = WatchFaceCatalogAdapter(catalog) { selectedItem ->
            // Item selected - adapter handles visual selection
        }
        recyclerView.adapter = adapter

        builder.setPositiveButton(
            when (action) {
                WatchChangeWatchDialAction.Add -> "Upload Watchface"
                WatchChangeWatchDialAction.Edit -> "Change Watchface"
            }
        ) { _, _ ->
            adapter?.getSelectedItem()?.let { selectedWatchface ->
                listener?.onWatchDialSet(selectedWatchface)
            }
        }

        // Set up negative button to cancel
        builder.setNegativeButton("Cancel") { _, _ -> dismiss() }
        builder.setView(view)
        return builder.create()
    }

    fun addListener(listener: OnWatchDialSetListener) {
        assert(this.listener == null)
        this.listener = listener
    }
}