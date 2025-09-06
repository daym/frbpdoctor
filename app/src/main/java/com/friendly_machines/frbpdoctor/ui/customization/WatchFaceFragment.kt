package com.friendly_machines.frbpdoctor.ui.customization

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.fr_yhe_api.commondata.WatchChangeWatchDialAction
import com.friendly_machines.fr_yhe_api.commondata.WatchDialDataBlock
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponseType
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.WatchCommunicationClientShorthand

class WatchFaceFragment : Fragment() {
    private var recyclerView: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_watch_face, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addWatchDialButton = view.findViewById<Button>(R.id.addWatchDialButton)
        addWatchDialButton.setOnClickListener {
            val editWatchFaceDialog = EditWatchFaceDialog(WatchChangeWatchDialAction.Add)
            editWatchFaceDialog.addListener(object : EditWatchFaceDialog.OnWatchDialSetListener {
                override fun onWatchDialSet(watchface: WatchfaceCatalogItem) {
                    // Launch watchface download
                    launchWatchfaceUpload(watchface)
                }
            })
            editWatchFaceDialog.show(childFragmentManager, "edit_watch_dial_dialog")
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        //adapter.notifyDataSetChanged()
        this.recyclerView = recyclerView

        val chooseWatchDialButton = view.findViewById<Button>(R.id.chooseWatchDialButton)
        chooseWatchDialButton.setOnClickListener {
            if (recyclerView != null && recyclerView.adapter != null) { // FIXME
                val id = (recyclerView!!.adapter as WatchFaceAdapter).getSelectedItemId()
                WatchCommunicationClientShorthand.bindExecOneCommandUnbind(requireContext(), WatchResponseType.ChangeWatchDial) { binder ->
                    id?.let {
                        binder.selectWatchFace(it)
                    }
                }
            }
        }

        WatchCommunicationClientShorthand.bindExecOneCommandUnbind(requireContext(), WatchResponseType.GetWatchDials) { binder ->
            binder.getWatchDial()
        }
    }

    fun setData(data: List<WatchDialDataBlock>) {
        val adapter = WatchFaceAdapter(data.sortedBy { it.id })
        recyclerView!!.adapter = adapter
        adapter.notifyDataSetChanged()
    }
    
    private fun launchWatchfaceUpload(watchface: WatchfaceCatalogItem) {
        // Load the .bin file for the selected watchface
        val binData = WatchfaceCatalogLoader.loadWatchfaceBinary(requireContext(), watchface)
        if (binData == null) {
            // TODO: Show error toast
            return
        }
        
        // Launch the WatchFaceDownloadingFragment with the watchface data
        val fragment = WatchFaceDownloadingFragment.newInstance(
            mtu = 235.toByte(), // Standard BLE MTU - 9 bytes overhead = 235
            dialPlateId = watchface.getDialPlateIdInt(),
            blockNumber = 0, // Default for new uploads
            version = 1, // Default version
            body = binData
        )
        
        // Replace current fragment with downloading fragment
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, fragment)
            .addToBackStack(null)
            .commit()
    }
}