package com.friendly_machines.frbpdoctor.ui.health

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.watchprotocol.notification.big.SportDataBlock

class SportFragment : Fragment() {

    private var recyclerView: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sport, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        //val adapter = SportAdapter(data)
        //recyclerView.adapter = adapter
        //adapter.notifyDataSetChanged()
        this.recyclerView = recyclerView
    }

    fun setData(data: Array<SportDataBlock>) {
        val adapter = SportAdapter(data.sortedBy { it.timestamp })
        recyclerView!!.adapter = adapter
        //adapter.notifyDataSetChanged()
    }

}