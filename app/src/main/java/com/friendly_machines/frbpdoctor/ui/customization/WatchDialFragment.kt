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

class WatchDialFragment : Fragment() {
    private var recyclerView: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_watch_dial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addWatchDialButton = view.findViewById<Button>(R.id.addWatchDialButton)
        addWatchDialButton.setOnClickListener {
            val editWatchDialDialog = EditWatchDialDialog(WatchChangeWatchDialAction.Add)
            editWatchDialDialog.addListener(object : EditWatchDialDialog.OnWatchDialSetListener {
                override fun onWatchDialSet() {
                    WatchCommunicationClientShorthand.bindExecOneCommandUnbind(requireContext(), WatchResponseType.ChangeWatchDial) { binder ->
                        binder.getWatchDial()
                    }
                    // TODO refresh watch_dial list maybe
                }
            })
            editWatchDialDialog.show(childFragmentManager, "edit_watch_dial_dialog")
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        //adapter.notifyDataSetChanged()
        this.recyclerView = recyclerView

        val chooseWatchDialButton = view.findViewById<Button>(R.id.chooseWatchDialButton)
        chooseWatchDialButton.setOnClickListener {
            WatchCommunicationClientShorthand.bindExecOneCommandUnbind(requireContext(), WatchResponseType.ChangeWatchDial) { binder ->
                val id = (recyclerView!!.adapter as WatchDialAdapter).getSelectedItemId()
                id?.let {
                    binder.selectWatchDial(it)
                }
            }
        }

        WatchCommunicationClientShorthand.bindExecOneCommandUnbind(requireContext(), WatchResponseType.GetWatchDials) { binder ->
            binder.getWatchDial()
        }
    }

    fun setData(data: List<WatchDialDataBlock>) {
        val adapter = WatchDialAdapter(data.sortedBy { it.id })
        recyclerView!!.adapter = adapter
        adapter.notifyDataSetChanged()
    }
}