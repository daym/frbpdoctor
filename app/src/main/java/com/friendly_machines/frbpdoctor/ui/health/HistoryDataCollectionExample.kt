package com.friendly_machines.frbpdoctor.ui.health

import android.content.Context
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchBinder
import com.friendly_machines.frbpdoctor.R
import kotlinx.coroutines.launch

/**
 * Example integration showing how to use the history controllers.
 * This demonstrates the pattern for collecting data using the new controller system.
 */
class HistoryDataCollectionExample {

    companion object {
        private const val TAG = "HistoryDataCollection"
    }

    /**
     * Example: Collect blood pressure history using the new controller system.
     * 
     * The BloodHistoryController now:
     * - Has a private constructor (can't be instantiated directly)
     * - Uses a companion object collect() method
     * - Manages its own lifecycle and registry
     * - Handles fragment updates and HealthConnect insertion internally
     */
    fun collectBloodPressureHistory(
        activity: HealthActivity,
        context: Context,
        recyclerView: RecyclerView?,
        binder: IWatchBinder
    ) {
        activity.lifecycleScope.launch {
            // Simply call the companion object's collect method
            // It handles everything: registry check, controller creation, lifecycle
            BloodHistoryController.collect(
                binder = binder,
                context = context,
                recyclerView = recyclerView,
                onProgress = { current, total, message ->
                    Log.d(TAG, "Blood pressure collection progress: $current/$total - $message")
                    // Update UI progress bar here
                },
                onComplete = {
                    Log.d(TAG, "Blood pressure collection complete")
                    // The controller has already updated the RecyclerView
                    // and inserted records into HealthConnect
                },
                onError = { error ->
                    Log.e(TAG, "Blood pressure collection failed", error)
                    // Show error to user
                }
            )
        }
    }

    /**
     * Example: How fragments should use the controllers.
     * 
     * This is the pattern used in BloodPressureFragment:
     * 1. Get the binder from HealthActivity's shared service connection
     * 2. Call the controller's collect() method
     * 3. Handle UI updates via callbacks
     */
    fun fragmentUsageExample(fragment: BloodPressureFragment) {
        fragment.lifecycleScope.launch {
            // Get the shared binder from the activity
            val activity = fragment.requireActivity() as? HealthActivity
            val binder = activity?.watchBinder
            
            if (binder != null) {
                // Use the controller's companion object method
                BloodHistoryController.collect(
                    binder = binder,
                    context = fragment.requireContext(),
                    recyclerView = fragment.view?.findViewById(R.id.list),
                    onProgress = { current, total, message ->
                        // Update progress UI
                    },
                    
                    onComplete = {
                        // Collection complete, data already in RecyclerView
                    },
                    onError = { error ->
                        // Handle error
                    }
                )
            }
        }
    }

    /**
     * Example: What happens internally in the controller.
     * 
     * The controller's collect() method:
     * 1. Checks with HistoryControllerRegistry if it can run
     * 2. Creates a private instance of the controller
     * 3. Adds it as a listener to the binder
     * 4. Runs the collection process
     * 5. Updates the RecyclerView in real-time as data arrives
     * 6. Inserts records into HealthConnect
     * 7. Cleans up the listener
     * 8. Updates the registry that it's done
     * 
     * All of this is handled internally - the caller just gets callbacks.
     */
    fun internalFlowExplanation() {
        // This is what happens inside BloodHistoryController.collect():
        //
        // if (!HistoryControllerRegistry.startOperation(WatchHGetBloodHistoryCommand::class)) {
        //     onError(Exception("Already running"))
        //     return
        // }
        //
        // try {
        //     val controller = BloodHistoryController(...) // private constructor
        //     val disconnector = binder.addListener(controller)
        //     
        //     try {
        //         controller.collectHistory() // runs the collection
        //     } finally {
        //         disconnector?.removeListener(disconnector)
        //     }
        // } finally {
        //     HistoryControllerRegistry.completeOperation(WatchHGetBloodHistoryCommand::class)
        // }
    }

    /**
     * Note: The old pattern with HistoryControllerFactory is obsolete.
     * 
     * OLD (no longer works):
     * val controller = HistoryControllerFactory.createBloodHistoryController(...)
     * 
     * NEW:
     * BloodHistoryController.collect(...)
     * 
     * Benefits of the new pattern:
     * - Enforces singleton behavior (only one collection at a time)
     * - Encapsulates all domain logic in the controller
     * - Simpler API for fragments
     * - No risk of forgetting cleanup
     * - Controller manages its own lifecycle
     */
}