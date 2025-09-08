package com.friendly_machines.frbpdoctor.ui.health

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.fr_yhe_api.commondata.HSleepDataBlock
import com.friendly_machines.fr_yhe_api.commondata.SleepDataBlockMed
import com.friendly_machines.fr_yhe_api.commondata.Timestamp2000
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchBinder
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.command.WatchHGetSleepHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHHistoryBlockCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHDeleteSleepHistoryCommand
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.coroutineScope
import kotlin.reflect.KClass

/**
 * Controller for collecting sleep history data.
 */
class SleepHistoryController private constructor(
    binder: IWatchBinder,
    context: Context,
    recyclerView: RecyclerView?,
    onProgress: (current: Int, total: Int, message: String) -> Unit,
    onComplete: () -> Unit,
    onError: (error: Exception) -> Unit
) : HistoryController<HSleepDataBlock>(binder, context, recyclerView, onProgress, onComplete, onError) {
    override val responseTypes: Set<KClass<out WatchResponse>> = RESPONSE_TYPES
    override val medBigResponseTypes: Set<KClass<out com.friendly_machines.fr_yhe_med.WatchBigResponseMed>> = MED_BIG_RESPONSE_TYPES

    companion object {
        private const val TAG = "SleepHistoryController"
        val RESPONSE_TYPES: Set<KClass<out WatchResponse>> = setOf(
            WatchHGetSleepHistoryCommand.Response::class,
            WatchHHistoryBlockCommand.Response::class,
            WatchHDeleteSleepHistoryCommand.Response::class
        )
        val MED_BIG_RESPONSE_TYPES: Set<KClass<out com.friendly_machines.fr_yhe_med.WatchBigResponseMed>> = setOf(
            com.friendly_machines.fr_yhe_med.WatchBigResponseMed.GetSleepData::class
        )

        /**
         * Create a new sleep history controller.
         * Returns null if another controller is already active for these response types.
         */
        suspend fun collect(
            binder: IWatchBinder,
            context: Context,
            recyclerView: RecyclerView?,
            onProgress: (current: Int, total: Int, message: String) -> Unit,
            onComplete: () -> Unit,
            onError: (error: Exception) -> Unit
        ): SleepHistoryController? {
            // Atomically check and register
            if (!HistoryControllerRegistry.startOperation(RESPONSE_TYPES, MED_BIG_RESPONSE_TYPES)) {
                return null
            }
            
            return try {
                val result = SleepHistoryController(binder, context, recyclerView, onProgress, onComplete, onError)
                result.startCollection()
                result
            } catch (e: Exception) {
                HistoryControllerRegistry.completeOperation(RESPONSE_TYPES, MED_BIG_RESPONSE_TYPES)
                throw e
            }
        }
    }

    /**
     * Update the fragment's RecyclerView with collected sleep data
     */
    private fun updateFragmentData() {
        recyclerView?.post {
            // Use HSleepDataBlock directly for UI - it has proper Timestamp2000 support
            val adapter = SleepAdapter(collectedItems.sortedBy { it.startTime })
            recyclerView.adapter = adapter
        }
    }

    /**
     * Start the sleep history collection process with synchronous-looking loop body
     */
    override suspend fun startCollection() = coroutineScope {
        try {
            onProgress(0, 0, "Starting sleep history collection...")
            collectedItems.clear()

            // Start the sleep history command
            // FIXME: binder.getSleepHistory(1701730800, 1702162800) // TODO: Make parameters configurable

            var currentCount = 0
            var expectedTotal = 0

            // Collect bulk responses
            while (true) {
                val response = responseChannel.receive()

                when (response) {
                    is WatchHGetSleepHistoryCommand.Response -> {
                        // Update expected total on first response
                        if (expectedTotal == 0) {
                            expectedTotal = response.items.size
                        }

                        // Process each sleep item
                        response.items.forEach { item ->
                            collectedItems.add(item)
                            currentCount++
                            onProgress(currentCount, expectedTotal, "Received sleep record")
                            // Update UI after each item for real-time feedback
                            updateFragmentData()
                        }
                    }

                    is com.friendly_machines.fr_yhe_med.WatchBigResponseMed.GetSleepData -> {
                        // Convert SleepDataBlock to HSleepDataBlock format for consistent processing
                        response.data.forEach { sleepData ->
                            val hSleepItem = HSleepDataBlock(
                                startTime = Timestamp2000.fromUnixMillis(sleepData.startTimestamp.toLong() * 1000L),
                                endTime = Timestamp2000.fromUnixMillis(sleepData.endTimestamp.toLong() * 1000L),
                                deepSleepCount = 0U.toShort(), // FIXME: not available in SleepDataBlock
                                lightSleepCount = 0U.toShort(), // FIXME: not available in SleepDataBlock
                                deepSleepTotal = 0U.toShort(), // FIXME: calculate from quality and duration
                                lightSleepTotal = (sleepData.endTimestamp - sleepData.startTimestamp).toShort(), // FIXME: terrible
                                sleepSegments = listOf(), // FIXME: terrible
                                wakeCount = 0, // FIXME: terrible
                                wakeDuration = 0, // FIXME: terrible
                            )
                            collectedItems.add(hSleepItem)
                            currentCount++
                            onProgress(currentCount, response.data.size, "Received sleep record")
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
            binder.deleteSleepHistory()
            
            // Wait for delete confirmation
            val deleteResponse = responseChannel.receive()
            if (deleteResponse is WatchHDeleteSleepHistoryCommand.Response) {
                if (deleteResponse.status == 0.toByte()) {
                    android.util.Log.d(TAG, "Watch confirmed deletion of synced sleep data")
                } else {
                    android.util.Log.w(TAG, "Watch delete confirmation failed with status: ${deleteResponse.status}")
                }
            }

            // Complete
            onProgress(expectedTotal, expectedTotal, "Sleep sync completed successfully")
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