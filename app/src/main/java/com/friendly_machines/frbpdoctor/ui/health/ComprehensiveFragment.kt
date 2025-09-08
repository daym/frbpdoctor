package com.friendly_machines.frbpdoctor.ui.health

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.fr_yhe_api.commondata.HHistoryComprehensiveMeasurementDataBlock
import com.friendly_machines.frbpdoctor.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ComprehensiveFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView
    private lateinit var syncButton: Button
    private val fragmentScope = CoroutineScope(Dispatchers.Main + Job())
    
    companion object {
        private const val TAG = "ComprehensiveFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_comprehensive, container, false)
        
        recyclerView = view.findViewById(R.id.comprehensiveRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        statusText = view.findViewById(R.id.statusText)
        syncButton = view.findViewById(R.id.syncButton)
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        syncButton.setOnClickListener {
            startComprehensiveSync()
        }
        
        return view
    }

    private fun startComprehensiveSync() {
        val healthActivity = activity as? HealthActivity
        val binder = healthActivity?.watchBinder
        
        if (binder == null) {
            statusText.text = "Watch not connected"
            return
        }

        fragmentScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                syncButton.isEnabled = false
                statusText.text = "Starting comprehensive measurement sync..."
                
                val controller = ComprehensiveMeasurementHistoryController.collect(
                    binder = binder,
                    context = requireContext(),
                    recyclerView = recyclerView,
                    onProgress = { current, total, message ->
                        activity?.runOnUiThread {
                            statusText.text = if (total > 0) {
                                "$message ($current/$total)"
                            } else {
                                message
                            }
                        }
                    },
                    onComplete = {
                        activity?.runOnUiThread {
                            progressBar.visibility = View.GONE
                            syncButton.isEnabled = true
                            statusText.text = "Comprehensive measurement sync completed successfully"
                        }
                    },
                    onError = { error ->
                        activity?.runOnUiThread {
                            progressBar.visibility = View.GONE
                            syncButton.isEnabled = true
                            statusText.text = "Sync failed: ${error.message}"
                            Log.e(TAG, "Comprehensive measurement sync failed", error)
                        }
                    }
                )
                
                if (controller == null) {
                    activity?.runOnUiThread {
                        progressBar.visibility = View.GONE
                        syncButton.isEnabled = true
                        statusText.text = "Sync already in progress"
                    }
                }
                
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    progressBar.visibility = View.GONE
                    syncButton.isEnabled = true
                    statusText.text = "Failed to start sync: ${e.message}"
                    Log.e(TAG, "Failed to start comprehensive measurement sync", e)
                }
            }
        }
    }

    // Data is now managed directly by ComprehensiveMeasurementHistoryController - no manual setData needed

    override fun onDestroy() {
        super.onDestroy()
        fragmentScope.coroutineContext[Job]?.cancel()
    }
}