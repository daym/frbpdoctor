package com.friendly_machines.frbpdoctor.ui.health

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.watchprotocol.notification.big.HeatDataBlock

class HeatFragment : Fragment() {

    private var recyclerView: RecyclerView? = null

    companion object {
        fun newInstance() = HeatFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_heat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
//        val data = mutableListOf<SleepDataBlock>()
//        val adapter = HeatAdapter(data)
//        recyclerView.adapter = adapter
        //adapter.notifyDataSetChanged()
        this.recyclerView = recyclerView
    }

    fun setData(data: Array<HeatDataBlock>) {
        val adapter = HeatAdapter(data.sortedBy { it.dayTimestamp })
        recyclerView!!.adapter = adapter
        //adapter.notifyDataSetChanged()
    }

}