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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.fr_yhe_api.commondata.SportState
import com.friendly_machines.fr_yhe_api.commondata.SportType
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponseType
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.WatchCommunicationClientShorthand

class SportFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private lateinit var sportTypeSpinner: Spinner
    private lateinit var playPauseButton: Button
    private lateinit var stopButton: Button
    
    private var currentSportState: SportState = SportState.STOP
    private var currentSportType: SportType = SportType.WALKING

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sport, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI components
        sportTypeSpinner = view.findViewById(R.id.sportTypeSpinner)
        playPauseButton = view.findViewById(R.id.playPauseButton)
        stopButton = view.findViewById(R.id.stopButton)
        
        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        this.recyclerView = recyclerView

        // Set up sport type spinner
        setupSportTypeSpinner()
        
        // Set up button listeners
        setupButtonListeners()
        
        // Update button states
        updateButtonStates()
    }

    private fun setupSportTypeSpinner() {
        val sportTypes = SportType.values().map { it.name.replace('_', ' ') }
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, sportTypes)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        sportTypeSpinner.adapter = adapter
        
        // Set default selection
        sportTypeSpinner.setSelection(SportType.WALKING.ordinal)
    }

    private fun setupButtonListeners() {
        playPauseButton.setOnClickListener {
            val selectedSportType = SportType.values()[sportTypeSpinner.selectedItemPosition]
            
            when (currentSportState) {
                SportState.STOP -> {
                    // Start sport mode
                    setSportMode(SportState.START, selectedSportType)
                }
                SportState.START -> {
                    // Pause sport mode
                    setSportMode(SportState.PAUSE, selectedSportType)
                }
                SportState.PAUSE -> {
                    // Continue sport mode
                    setSportMode(SportState.CONTINUE, selectedSportType)
                }
                SportState.CONTINUE -> {
                    // Pause sport mode
                    setSportMode(SportState.PAUSE, selectedSportType)
                }
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
        
        // Update local state and UI
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
                sportTypeSpinner.isEnabled = true // Can change sport type when stopped
            }
            SportState.START -> {
                playPauseButton.text = "⏸ PAUSE"
                playPauseButton.isEnabled = true
                stopButton.isEnabled = true
                sportTypeSpinner.isEnabled = false // Cannot change sport type while running
            }
            SportState.PAUSE -> {
                playPauseButton.text = "▶ CONTINUE"
                playPauseButton.isEnabled = true
                stopButton.isEnabled = true
                sportTypeSpinner.isEnabled = false // Cannot change sport type while paused
            }
            SportState.CONTINUE -> {
                playPauseButton.text = "⏸ PAUSE"
                playPauseButton.isEnabled = true
                stopButton.isEnabled = true
                sportTypeSpinner.isEnabled = false // Cannot change sport type while running
            }
        }
    }

    // Method to handle watch-initiated state changes
    fun onSportStateChanged(sportState: SportState, sportType: SportType) {
        currentSportState = sportState
        currentSportType = sportType
        
        // Update spinner selection if sport type changed
        sportTypeSpinner.setSelection(sportType.ordinal)
        
        updateButtonStates()
    }

    fun setData(data: Array<com.friendly_machines.fr_yhe_api.commondata.SportDataBlock>) {
        val adapter = SportAdapter(data.sortedBy { it.timestamp })
        recyclerView!!.adapter = adapter
    }
}