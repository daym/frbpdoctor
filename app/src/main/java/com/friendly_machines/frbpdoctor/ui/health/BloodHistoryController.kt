package com.friendly_machines.frbpdoctor.ui.health

import android.content.Context
import android.util.Log
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.units.Pressure
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.fr_yhe_api.commondata.BpDataBlockMed
import com.friendly_machines.fr_yhe_api.commondata.HBloodDataBlock
import com.friendly_machines.fr_yhe_api.commondata.Timestamp2000
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchBinder
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.command.WatchHGetBloodHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHHistoryBlockCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHDeleteBloodHistoryCommand
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.coroutineScope
import kotlin.reflect.KClass

/**
 * Controller for collecting blood pressure history data.
 * Handles:
 * - Collecting data from watch
 * - Updating fragment's RecyclerView
 * - Inserting records into HealthConnect
 */
class BloodHistoryController private constructor(
    binder: IWatchBinder,
    context: Context,
    recyclerView: RecyclerView?,
    onProgress: (current: Int, total: Int, message: String) -> Unit,
    onComplete: () -> Unit,
    onError: (error: Exception) -> Unit
) : HistoryController<HBloodDataBlock>(binder, context, recyclerView, onProgress, onComplete, onError) {

    companion object {
        private const val TAG = "BloodHistoryController"
        private var activeController: BloodHistoryController? = null
        
        val RESPONSE_TYPES: Set<KClass<out WatchResponse>> = setOf(
            WatchHGetBloodHistoryCommand.Response::class,
            WatchHHistoryBlockCommand.Response::class,
            WatchHDeleteBloodHistoryCommand.Response::class
        )
        
        val MED_BIG_RESPONSE_TYPES: Set<KClass<out com.friendly_machines.fr_yhe_med.WatchBigResponseMed>> = setOf(
            com.friendly_machines.fr_yhe_med.WatchBigResponseMed.GetBpData::class
        )
        

        /**
         * Create a new blood pressure history controller.
         * Returns null if another controller is already active for these response types.
         */
        suspend fun collect(
            binder: IWatchBinder,
            context: Context,
            recyclerView: RecyclerView?,
            onProgress: (current: Int, total: Int, message: String) -> Unit,
            onComplete: () -> Unit,
            onError: (error: Exception) -> Unit
        ): BloodHistoryController? {
            // Atomically check and register
            if (!HistoryControllerRegistry.startOperation(RESPONSE_TYPES, MED_BIG_RESPONSE_TYPES)) {
                return null
            }
            
            return try {
                val result = BloodHistoryController(binder, context, recyclerView, onProgress, onComplete, onError)
                result.startCollection()
                result
            } catch (e: Exception) {
                HistoryControllerRegistry.completeOperation(RESPONSE_TYPES, MED_BIG_RESPONSE_TYPES)
                throw e
            }
        }
    }

    override val responseTypes: Set<KClass<out WatchResponse>> = RESPONSE_TYPES
    override val medBigResponseTypes: Set<KClass<out com.friendly_machines.fr_yhe_med.WatchBigResponseMed>> = MED_BIG_RESPONSE_TYPES

    /**
     * Update the fragment's RecyclerView with new blood pressure data
     */
    private fun updateFragmentData() {
        recyclerView?.post {
            // Use HBloodDataBlock directly for UI - it has proper Timestamp2000 support
            val adapter = BloodPressureAdapter(collectedItems.sortedBy { it.bloodStartTime })
            recyclerView.adapter = adapter
        }
    }

    /**
     * Insert blood pressure records into HealthConnect
     */
    private suspend fun insertIntoHealthConnect() {
        try {
            val records = collectedItems.map { item ->
                BloodPressureRecord(
                    time = item.bloodStartTime.toInstant(),
                    zoneOffset = null,
                    systolic = Pressure.millimetersOfMercury(item.bloodSystolicPressure.toDouble()),
                    diastolic = Pressure.millimetersOfMercury(item.bloodDiastolicPressure.toDouble())
                )
            }
            
            healthClient.insertRecords(records)
            Log.d(TAG, "Inserted ${records.size} blood pressure records into HealthConnect")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert records into HealthConnect", e)
        }
    }

    /**
     * Collect blood pressure history data from the watch
     */
    override suspend fun startCollection() = coroutineScope {
        try {
            onProgress(0, 0, "Starting blood pressure collection...")
            collectedItems.clear()

            // Start the blood history command
            binder.getBpData()

            var currentCount = 0
            var expectedTotal = 100 // FIXME

            // Collect bulk responses
            while (true) {
                val response = responseChannel.receive()

                when (response) {
                    is WatchHGetBloodHistoryCommand.Response -> {
                        // Update expected total on first response
                        if (expectedTotal == 0) {
                            expectedTotal = response.items.size
                        }

                        // Process each blood pressure item
                        response.items.forEach { item ->
                            collectedItems.add(item)
                            currentCount++
                            onProgress(currentCount, expectedTotal, "Received blood pressure record")
                            // Update UI after each item for real-time feedback
                            updateFragmentData()
                        }
                    }

                    is com.friendly_machines.fr_yhe_med.WatchBigResponseMed.GetBpData -> {
                        // Convert BpDataBlock to HBloodDataBlock format for consistent processing
                        response.data.forEach { bpData ->
                            val hBloodItem = HBloodDataBlock(
                                bloodSystolicPressure = bpData.systolicPressure,
                                bloodDiastolicPressure = bpData.diastolicPressure,
                                bloodStartTime = Timestamp2000.fromUnixMillis(bpData.timestamp.toLong() * 1000L),
                                isInflated = 0,
                                reserved = 0,
                            )
                            collectedItems.add(hBloodItem)
                            currentCount++
                            onProgress(currentCount, response.data.size, "Received blood pressure record")
                            updateFragmentData()
                        }
                    }

                    is WatchHHistoryBlockCommand.Response -> {
                        if (response.status == 0.toByte()) {
                            // Successfully received all data
                            break
                        } else {
                            throw Exception("History block command failed with status: ${response.status}")
                        }
                    }
                }
            }

            // Insert into HealthConnect first
            insertIntoHealthConnect()
            
            // Send delete acknowledgment to watch (tells watch to delete synced data)
            onProgress(currentCount, expectedTotal, "Acknowledging data receipt to watch...")
            binder.deleteBloodHistory()
            
            // Wait for delete confirmation
            val deleteResponse = responseChannel.receive()
            if (deleteResponse is WatchHDeleteBloodHistoryCommand.Response) {
                if (deleteResponse.status == 0.toByte()) {
                    Log.d(TAG, "Watch confirmed deletion of synced blood pressure data")
                } else {
                    Log.w(TAG, "Watch delete confirmation failed with status: ${deleteResponse.status}")
                }
            }

            // Complete
            onProgress(expectedTotal, expectedTotal, "Blood pressure sync completed successfully")
            onComplete()

        } catch (e: ClosedReceiveChannelException) {
            // "normal"
        } catch (e: Exception) {
            onError(e)
        } finally {
            HistoryControllerRegistry.completeOperation(RESPONSE_TYPES, MED_BIG_RESPONSE_TYPES)
        }
    }
}