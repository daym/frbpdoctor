package com.friendly_machines.frbpdoctor.ui.health

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.frbpdoctor.R
import kotlinx.coroutines.launch

class HeartRateFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var syncButton: Button
    private var activeController: HeartRateHistoryController? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_heart_rate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById<RecyclerView>(R.id.list).apply {
            layoutManager = LinearLayoutManager(requireContext())
        }
        syncButton = view.findViewById<Button>(R.id.syncButton).apply {
            setOnClickListener { toggleSync() }
        }
        updateButtonState()
    }

    private fun toggleSync() {
        activeController?.let { controller ->
            controller.close()
            activeController = null
        } ?: lifecycleScope.launch {
            val activity = requireActivity() as HealthActivity
            val controller = HeartRateHistoryController.collect(
                binder = activity.watchBinder!!,
                context = requireContext(),
                recyclerView = recyclerView,
                onProgress = { _, _, _ -> updateButtonState() },
                onComplete = { 
                    activeController = null
                    updateButtonState() 
                },
                onError = { 
                    activeController = null
                    updateButtonState() 
                }
            )
            if (controller != null) {
                activeController = controller
                updateButtonState()
            }
        }
    }

    private fun updateButtonState() {
        val isActive = HistoryControllerRegistry.isOperationActive(
            HeartRateHistoryController.RESPONSE_TYPES,
            HeartRateHistoryController.MED_BIG_RESPONSE_TYPES
        )
        syncButton.text = if (isActive) "Cancel" else "Sync"
    }
}