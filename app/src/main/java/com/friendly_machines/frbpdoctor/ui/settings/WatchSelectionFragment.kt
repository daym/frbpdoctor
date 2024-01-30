package com.friendly_machines.frbpdoctor.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.frbpdoctor.R
import com.polidea.rxandroidble3.scan.ScanResult

class WatchSelectionFragment(private val resultListener: ScannerResultListener) : /* ListFragment */ DialogFragment(), ScannerRecyclerViewAdapter.ItemClickListener {
    private lateinit var adapter: ScannerRecyclerViewAdapter
    private val scanResults: MutableList<ScanResult> = mutableListOf()

    interface ScannerResultListener {
        fun onScanningUserSelectedDevice(scanResult: ScanResult)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_scanner, container, false)
        val that = this
        val list = view.findViewById<RecyclerView>(R.id.list)
        with(list) {
            that.adapter = ScannerRecyclerViewAdapter(scanResults, that)
            adapter = that.adapter
        }
        return view
    }

    companion object {
        const val TAG = "ScannerFragment"
    }

    override fun onItemClick(position: Int) {
        val scanResult = scanResults[position]
        resultListener.onScanningUserSelectedDevice(scanResult)
        dismiss()  // Close the dialog
    }
}