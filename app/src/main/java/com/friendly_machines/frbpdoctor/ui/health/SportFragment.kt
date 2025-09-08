package com.friendly_machines.frbpdoctor.ui.health

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponseType
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.WatchCommunicationClientShorthand
import kotlinx.coroutines.launch

class SportFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private lateinit var setStepGoalButton: Button
    
    // Sync controls
    private lateinit var syncButton: Button
    private var activeController: SportHistoryController? = null
    
    companion object {
        private const val TAG = "SportFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sport, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI components
        setStepGoalButton = view.findViewById(R.id.setStepGoalButton)
        
        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        this.recyclerView = recyclerView

        // Initialize sync controls
        syncButton = view.findViewById<Button>(R.id.syncButton).apply {
            setOnClickListener { toggleSync() }
        }

        // Set up button listeners
        setupStepGoalButton()
        
        // Update button states
        updateSyncButtonState()
    }


    private fun setupStepGoalButton() {
        setStepGoalButton.setOnClickListener {
            val stepGoalEditText = EditText(requireContext())
            stepGoalEditText.inputType = InputType.TYPE_CLASS_NUMBER
            stepGoalEditText.hint = "Number of steps"

            AlertDialog.Builder(requireContext())
                .setTitle("Your target number of steps")
                .setMessage("What number of steps do you target?") // TODO translate
                .setView(stepGoalEditText)
                .setPositiveButton("Set Step Goal") { _, _ ->
                    val stepGoalText = stepGoalEditText.text.toString()
                    try {
                        val stepGoal = Integer.parseInt(stepGoalText)
                        WatchCommunicationClientShorthand.bindExecOneCommandUnbind(
                            requireContext(), 
                            WatchResponseType.SetStepGoal
                        ) { binder ->
                            binder.setStepGoal(stepGoal)
                        }
                        Toast.makeText(requireContext(), "Step goal set to $stepGoal", Toast.LENGTH_SHORT).show()
                    } catch (e: NumberFormatException) {
                        Toast.makeText(requireContext(), "Number of steps wasn't a positive number", Toast.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton("Cancel") { _, _ -> }
                .show()
        }
    }


    // Data is now managed directly by SportHistoryController - no manual setData needed

    
    private fun toggleSync() {
        activeController?.let { controller ->
            controller.close()
            activeController = null
        } ?: lifecycleScope.launch {
            val activity = requireActivity() as HealthActivity
            val controller = SportHistoryController.collect(
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
            SportHistoryController.RESPONSE_TYPES, 
            SportHistoryController.MED_BIG_RESPONSE_TYPES
        )
        syncButton.text = if (isActive) "Cancel" else "Sync"
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}