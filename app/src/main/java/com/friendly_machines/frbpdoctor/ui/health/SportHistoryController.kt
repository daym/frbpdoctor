package com.friendly_machines.frbpdoctor.ui.health

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.fr_yhe_api.commondata.HSportDataBlock
import com.friendly_machines.fr_yhe_api.commondata.SportDataBlockMed
import com.friendly_machines.fr_yhe_api.commondata.Timestamp2000
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchBinder
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.command.WatchHGetSportHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHHistoryBlockCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHDeleteSportHistoryCommand
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.coroutineScope
import kotlin.reflect.KClass

/**
 * Controller for collecting sport history data.
 */
class SportHistoryController private constructor(
    binder: IWatchBinder,
    context: Context,
    recyclerView: RecyclerView?,
    onProgress: (current: Int, total: Int, message: String) -> Unit,
    onComplete: () -> Unit,
    onError: (error: Exception) -> Unit
) : HistoryController<HSportDataBlock>(binder, context, recyclerView, onProgress, onComplete, onError) {

    companion object {
        private const val TAG = "SportHistoryController"
        val RESPONSE_TYPES: Set<KClass<out WatchResponse>> = setOf(
            WatchHGetSportHistoryCommand.Response::class,
            WatchHHistoryBlockCommand.Response::class,
            WatchHDeleteSportHistoryCommand.Response::class
        )
        val MED_BIG_RESPONSE_TYPES: Set<KClass<out com.friendly_machines.fr_yhe_med.WatchBigResponseMed>> = setOf(
            com.friendly_machines.fr_yhe_med.WatchBigResponseMed.GetSportData::class,
            com.friendly_machines.fr_yhe_med.WatchBigResponseMed.GetStepData::class
        )

        /**
         * Create a new sport history controller.
         * Returns null if another controller is already active for these response types.
         */
        suspend fun collect(
            binder: IWatchBinder,
            context: Context,
            recyclerView: RecyclerView?,
            onProgress: (current: Int, total: Int, message: String) -> Unit,
            onComplete: () -> Unit,
            onError: (error: Exception) -> Unit
        ): SportHistoryController? {
            // Atomically check and register
            if (!HistoryControllerRegistry.startOperation(RESPONSE_TYPES, MED_BIG_RESPONSE_TYPES)) {
                return null
            }
            
            return try {
                val result = SportHistoryController(binder, context, recyclerView, onProgress, onComplete, onError)
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
     * Update the fragment's RecyclerView with collected sport data
     */
    private fun updateFragmentData() {
        recyclerView?.post {
            // Use HSportDataBlock directly for UI - it has proper Timestamp2000 support
            val adapter = SportAdapter(collectedItems.sortedBy { it.startTime })
            recyclerView.adapter = adapter
        }
    }

    /**
     * Start the sport history collection process with synchronous-looking loop body
     */
    override suspend fun startCollection() = coroutineScope {
        try {
            onProgress(0, 0, "Starting sport history collection...")
            collectedItems.clear()
            
            // Start the sport history command
            // FIXME: binder.getSportHistory()
            
            var currentCount = 0
            var expectedTotal = 0

            // Collect bulk responses
            while (true) {
                val response = responseChannel.receive()
                
                when (response) {
                    is WatchHGetSportHistoryCommand.Response -> {
                        // Update expected total on first response
                        if (expectedTotal == 0) {
                            expectedTotal = response.items.size
                        }
                        
                        // Process each sport item
                        response.items.forEach { item ->
                            collectedItems.add(item)
                            currentCount++
                            onProgress(currentCount, expectedTotal, "Received sport record")
                            // Update UI after each item for real-time feedback
                            updateFragmentData()
                        }
                    }
                    is com.friendly_machines.fr_yhe_med.WatchBigResponseMed.GetSportData -> {
                        // Convert SportDataBlock to HSportDataBlock format for consistent processing
                        response.data.forEach { sportData ->
                            val hSportItem = HSportDataBlock(
                                startTime = Timestamp2000.fromUnixMillis(sportData.timestamp.toLong() * 1000L),
                                endTime = Timestamp2000.fromUnixMillis((sportData.timestamp.toLong() + sportData.duration.toLong()) * 1000L),
                                distance = sportData.runningDistance,
                                steps = sportData.stepCount.toUShort(), // FIXME: terrible
                                calories = sportData.heat.toShort() // FIXME: heat vs calories conversion
                            )
                            collectedItems.add(hSportItem)
                            currentCount++
                            onProgress(currentCount, response.data.size, "Received sport record")
                            updateFragmentData()
                        }
                    }
                    is com.friendly_machines.fr_yhe_med.WatchBigResponseMed.GetStepData -> {
                        // Convert StepsDataBlock to HSportDataBlock format for consistent processing
                        response.data.forEach { stepData ->
                            val hSportItem = HSportDataBlock(
                                startTime = Timestamp2000.fromUnixMillis(stepData.dayTimestamp.toLong() * 1000L),
                                endTime = Timestamp2000.fromUnixMillis(stepData.dayTimestamp.toLong() * 1000L + 24*60*60*1000L), // End of day
                                distance = 0, // Not available in step data
                                steps = stepData.currentSteps.toUShort(),
                                calories = 0 // Not available in step data
                            )
                            collectedItems.add(hSportItem)
                            currentCount++
                            onProgress(currentCount, response.data.size, "Received step record")
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
            
            // Send delete acknowledgment to watch (tells watch to delete synced data)
            onProgress(currentCount, expectedTotal, "Acknowledging data receipt to watch...")
            binder.deleteSportHistory()
            
            // Wait for delete confirmation
            val deleteResponse = responseChannel.receive()
            if (deleteResponse is WatchHDeleteSportHistoryCommand.Response) {
                if (deleteResponse.status == 0.toByte()) {
                    android.util.Log.d(TAG, "Watch confirmed deletion of synced sport data")
                } else {
                    android.util.Log.w(TAG, "Watch delete confirmation failed with status: ${deleteResponse.status}")
                }
            }

            // Complete
            onProgress(expectedTotal, expectedTotal, "Sport sync completed successfully")
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