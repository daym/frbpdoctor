package com.friendly_machines.frbpdoctor.ui.health

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.fr_yhe_api.commondata.SportState
import com.friendly_machines.fr_yhe_api.commondata.SportType
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponseType
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.WatchCommunicationClientShorthand
import kotlinx.coroutines.launch

class SportModeFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var syncButton: Button
    private lateinit var sportTypeSpinner: Spinner
    private lateinit var playPauseButton: Button
    private lateinit var stopButton: Button
    private var activeController: SportModeHistoryController? = null
    
    private var currentSportState: SportState = SportState.STOP
    private var currentSportType: SportType = SportType.WALKING

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sport_mode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById<RecyclerView>(R.id.list).apply {
            layoutManager = LinearLayoutManager(requireContext())
        }
        syncButton = view.findViewById<Button>(R.id.syncButton).apply {
            setOnClickListener { toggleSync() }
        }
        
        // Sport mode controls
        sportTypeSpinner = view.findViewById(R.id.sportTypeSpinner)
        playPauseButton = view.findViewById(R.id.playPauseButton)
        stopButton = view.findViewById(R.id.stopButton)
        
        setupSportTypeSpinner()
        setupButtonListeners()
        updateButtonStates()
        updateSyncButtonState()
    }

    private fun setupSportTypeSpinner() {
        val sportTypes = SportType.values().map { it.name.replace('_', ' ') }
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, sportTypes)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        sportTypeSpinner.adapter = adapter
        sportTypeSpinner.setSelection(SportType.WALKING.ordinal)
    }

    private fun setupButtonListeners() {
        playPauseButton.setOnClickListener {
            val selectedSportType = SportType.values()[sportTypeSpinner.selectedItemPosition]
            
            when (currentSportState) {
                SportState.STOP -> setSportMode(SportState.START, selectedSportType)
                SportState.START -> setSportMode(SportState.PAUSE, selectedSportType)
                SportState.PAUSE -> setSportMode(SportState.CONTINUE, selectedSportType)
                SportState.CONTINUE -> setSportMode(SportState.PAUSE, selectedSportType)
            }
        }

        stopButton.setOnClickListener {
            val selectedSportType = SportType.values()[sportTypeSpinner.selectedItemPosition]
            setSportMode(SportState.STOP, selectedSportType)
        }
    }

    private fun setSportMode(sportState: SportState, sportType: SportType) {
        WatchCommunicationClientShorthand.bindExecOneCommandUnbind(requireContext(), WatchResponseType.SetSportMode) { binder ->
            binder.setSportMode(sportState, sportType)
        }
        
        currentSportState = sportState
        currentSportType = sportType
        updateButtonStates()
        
        Toast.makeText(requireContext(), "Sport mode: ${sportState.name} ${sportType.name}", Toast.LENGTH_SHORT).show()
    }

    private fun updateButtonStates() {
        when (currentSportState) {
            SportState.STOP -> {
                playPauseButton.text = "▶ START"
                playPauseButton.isEnabled = true
                stopButton.isEnabled = false
                sportTypeSpinner.isEnabled = true
            }
            SportState.START -> {
                playPauseButton.text = "⏸ PAUSE"
                playPauseButton.isEnabled = true
                stopButton.isEnabled = true
                sportTypeSpinner.isEnabled = false
            }
            SportState.PAUSE -> {
                playPauseButton.text = "▶ CONTINUE"
                playPauseButton.isEnabled = true
                stopButton.isEnabled = true
                sportTypeSpinner.isEnabled = false
            }
            SportState.CONTINUE -> {
                playPauseButton.text = "⏸ PAUSE"
                playPauseButton.isEnabled = true
                stopButton.isEnabled = true
                sportTypeSpinner.isEnabled = false
            }
        }
    }

    fun onSportStateChanged(sportState: SportState, sportType: SportType) {
        currentSportState = sportState
        currentSportType = sportType
        sportTypeSpinner.setSelection(sportType.ordinal)
        updateButtonStates()
    }

    private fun toggleSync() {
        activeController?.let { controller ->
            controller.close()
            activeController = null
        } ?: lifecycleScope.launch {
            val activity = requireActivity() as HealthActivity
            val controller = SportModeHistoryController.collect(
                binder = activity.watchBinder!!,
                context = requireContext(),
                recyclerView = recyclerView,
                onProgress = { _, _, _ -> updateSyncButtonState() },
                onComplete = { 
                    activeController = null
                    updateSyncButtonState() 
                },
                onError = { 
                    activeController = null
                    updateSyncButtonState() 
                }
            )
            if (controller != null) {
                activeController = controller
                updateSyncButtonState()
            }
        }
    }

    private fun updateSyncButtonState() {
        val isActive = HistoryControllerRegistry.isOperationActive(
            SportModeHistoryController.RESPONSE_TYPES, 
            SportModeHistoryController.MED_BIG_RESPONSE_TYPES
        )
        syncButton.text = if (isActive) "Cancel" else "Sync"
    }
}