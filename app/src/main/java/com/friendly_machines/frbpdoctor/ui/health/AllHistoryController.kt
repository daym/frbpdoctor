package com.friendly_machines.frbpdoctor.ui.health

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.fr_yhe_api.commondata.HHistoryAllDataBlock
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchBinder
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.command.WatchHGetAllHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHHistoryBlockCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHDeleteAllHistoryCommand
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.coroutineScope
import kotlin.reflect.KClass

/**
 * Controller for collecting all sensor history data (HR, SpO2, temperature, blood sugar).
 */
class AllHistoryController private constructor(
    binder: IWatchBinder,
    context: Context,
    recyclerView: RecyclerView?,
    onProgress: (current: Int, total: Int, message: String) -> Unit,
    onComplete: () -> Unit,
    onError: (error: Exception) -> Unit
) : HistoryController<HHistoryAllDataBlock>(binder, context, recyclerView, onProgress, onComplete, onError) {

    companion object {
        private const val TAG = "AllHistoryController"
        
        val RESPONSE_TYPES: Set<KClass<out WatchResponse>> = setOf(
            WatchHGetAllHistoryCommand.Response::class,
            WatchHHistoryBlockCommand.Response::class,
            WatchHDeleteAllHistoryCommand.Response::class
        )
        
        val MED_BIG_RESPONSE_TYPES: Set<KClass<out com.friendly_machines.fr_yhe_med.WatchBigResponseMed>> = setOf(
            // Add appropriate response types if they exist
        )

        suspend fun collect(
            binder: IWatchBinder,
            context: Context,
            recyclerView: RecyclerView?,
            onProgress: (current: Int, total: Int, message: String) -> Unit,
            onComplete: () -> Unit,
            onError: (error: Exception) -> Unit
        ): AllHistoryController? {
            if (!HistoryControllerRegistry.startOperation(RESPONSE_TYPES, MED_BIG_RESPONSE_TYPES)) {
                return null
            }
            
            return try {
                val result = AllHistoryController(binder, context, recyclerView, onProgress, onComplete, onError)
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

    override suspend fun startCollection() = coroutineScope {
        try {
            onProgress(0, 0, "Starting all sensor data collection...")
            collectedItems.clear()

            // Start the all history command
            binder.getAllHistoryData()

            var currentCount = 0
            var expectedTotal = 0

            while (true) {
                val response = responseChannel.receive()

                when (response) {
                    is WatchHGetAllHistoryCommand.Response -> {
                        if (expectedTotal == 0) {
                            expectedTotal = response.items.size
                        }

                        response.items.forEach { item ->
                            collectedItems.add(item)
                            currentCount++
                            onProgress(currentCount, expectedTotal, "Received all sensor record")
                        }
                    }

                    is WatchHHistoryBlockCommand.Response -> {
                        if (response.status == 0.toByte()) {
                            break
                        } else {
                            throw Exception("History block command failed with status: ${response.status}")
                        }
                    }
                }
            }

            // Send delete acknowledgment to watch
            onProgress(currentCount, expectedTotal, "Acknowledging data receipt to watch...")
            binder.deleteAllHistory()
            
            // Wait for delete confirmation
            val deleteResponse = responseChannel.receive()
            if (deleteResponse is WatchHDeleteAllHistoryCommand.Response) {
                if (deleteResponse.status == 0.toByte()) {
                    Log.d(TAG, "Watch confirmed deletion of synced all sensor data")
                } else {
                    Log.w(TAG, "Watch delete confirmation failed with status: ${deleteResponse.status}")
                }
            }

            onProgress(expectedTotal, expectedTotal, "All sensor data sync completed successfully")
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