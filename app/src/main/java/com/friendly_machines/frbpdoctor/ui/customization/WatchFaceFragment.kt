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
                override fun onWatchDialSet() {
                    WatchCommunicationClientShorthand.bindExecOneCommandUnbind(requireContext(), WatchResponseType.ChangeWatchDial) { binder ->
                        binder.getWatchDial()
                    }
                    // TODO refresh watch_dial list maybe
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
            WatchCommunicationClientShorthand.bindExecOneCommandUnbind(requireContext(), WatchResponseType.ChangeWatchDial) { binder ->
                val id = (recyclerView!!.adapter as WatchFaceAdapter).getSelectedItemId()
                id?.let {
                    binder.selectWatchFace(it)
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
}