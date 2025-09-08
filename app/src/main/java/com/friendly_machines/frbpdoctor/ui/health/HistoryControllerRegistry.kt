package com.friendly_machines.frbpdoctor.ui.health

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.command.WatchCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetBloodHistoryCommand
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KClass

/**
 * Registry for history controller operations.
 * Tracks which response types are currently active to ensure only one controller
 * can handle each response type at any time.
 * 
 * This prevents conflicts where multiple controllers try to
 * collect data simultaneously, which could cause misinterpretation of responses.
 */
internal object HistoryControllerRegistry {
    private val activeResponseTypes = mutableSetOf<KClass<out WatchResponse>>()
    private val activeMedBigResponseTypes = mutableSetOf<KClass<out com.friendly_machines.fr_yhe_med.WatchBigResponseMed>>()
    private val operationMutex = Mutex()
    
    /**
     * Attempt to start an operation for the given response types.
     * Atomically checks that NONE of the response types are active, then registers ALL of them.
     * 
     * @param responseTypes The set of WatchResponse types to register
     * @param medBigResponseTypes The set of MedBigResponse types to register  
     * @return true if operation started successfully, false if any response type already active
     */
    fun startOperation(
        responseTypes: Set<KClass<out WatchResponse>>, 
        medBigResponseTypes: Set<KClass<out com.friendly_machines.fr_yhe_med.WatchBigResponseMed>> = emptySet()
    ): Boolean {
        return synchronized(this) {
            // Check if any response types are already active
            if (responseTypes.any { activeResponseTypes.contains(it) } ||
                medBigResponseTypes.any { activeMedBigResponseTypes.contains(it) }) {
                false // At least one response type is already active
            } else {
                activeResponseTypes.addAll(responseTypes)
                activeMedBigResponseTypes.addAll(medBigResponseTypes)
                true
            }
        }
    }
    
    /**
     * Mark an operation as completed for the given response types.
     * Atomically removes all response types from the active set.
     * 
     * @param responseTypes The set of WatchResponse types to deregister
     * @param medBigResponseTypes The set of MedBigResponse types to deregister
     */
    fun completeOperation(
        responseTypes: Set<KClass<out WatchResponse>>,
        medBigResponseTypes: Set<KClass<out com.friendly_machines.fr_yhe_med.WatchBigResponseMed>>
    ) {
        synchronized(this) {
            activeResponseTypes.removeAll(responseTypes)
            activeMedBigResponseTypes.removeAll(medBigResponseTypes)
        }
    }
    
    /**
     * Check if any of the response types are currently active.
     * 
     * @param responseTypes The set of WatchResponse types to check
     * @param medBigResponseTypes The set of MedBigResponse types to check
     * @return true if any response type is currently active
     */
    fun isOperationActive(
        responseTypes: Set<KClass<out WatchResponse>>,
        medBigResponseTypes: Set<KClass<out com.friendly_machines.fr_yhe_med.WatchBigResponseMed>> = emptySet()
    ): Boolean {
        return synchronized(this) {
            responseTypes.any { activeResponseTypes.contains(it) } ||
            medBigResponseTypes.any { activeMedBigResponseTypes.contains(it) }
        }
    }
    
    /**
     * Get a list of currently active response types.
     * Useful for debugging or status reporting.
     */
    fun getActiveResponseTypes(): Set<KClass<out WatchResponse>> {
        return synchronized(activeResponseTypes) {
            activeResponseTypes.toSet()
        }
    }
}