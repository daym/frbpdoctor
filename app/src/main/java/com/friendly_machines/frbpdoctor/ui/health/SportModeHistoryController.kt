package com.friendly_machines.frbpdoctor.ui.health

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.fr_yhe_api.commondata.HHistorySportModeDataBlock
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchBinder
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.command.WatchHHistorySportModeCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHHistoryBlockCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHDeleteSportModeHistoryCommand
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.coroutineScope
import kotlin.reflect.KClass

/**
 * Controller for collecting sport mode session history data.
 */
class SportModeHistoryController private constructor(
    binder: IWatchBinder,
    context: Context,
    recyclerView: RecyclerView?,
    onProgress: (current: Int, total: Int, message: String) -> Unit,
    onComplete: () -> Unit,
    onError: (error: Exception) -> Unit
) : HistoryController<HHistorySportModeDataBlock>(binder, context, recyclerView, onProgress, onComplete, onError) {

    companion object {
        private const val TAG = "SportModeHistoryController"
        
        val RESPONSE_TYPES: Set<KClass<out WatchResponse>> = setOf(
            WatchHHistorySportModeCommand.Response::class,
            WatchHHistoryBlockCommand.Response::class,
            WatchHDeleteSportModeHistoryCommand.Response::class
        )
        
        val MED_BIG_RESPONSE_TYPES: Set<KClass<out com.friendly_machines.fr_yhe_med.WatchBigResponseMed>> = setOf()

        suspend fun collect(
            binder: IWatchBinder,
            context: Context,
            recyclerView: RecyclerView?,
            onProgress: (current: Int, total: Int, message: String) -> Unit,
            onComplete: () -> Unit,
            onError: (error: Exception) -> Unit
        ): SportModeHistoryController? {
            if (!HistoryControllerRegistry.startOperation(RESPONSE_TYPES, MED_BIG_RESPONSE_TYPES)) {
                return null
            }
            
            return try {
                val result = SportModeHistoryController(binder, context, recyclerView, onProgress, onComplete, onError)
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
            onProgress(0, 0, "Starting sport mode history collection...")
            collectedItems.clear()

            // Start the sport mode history command
            binder.getSportModeHistoryData()

            var currentCount = 0
            var expectedTotal = 0

            while (true) {
                val response = responseChannel.receive()

                when (response) {
                    is WatchHHistorySportModeCommand.Response -> {
                        if (expectedTotal == 0) {
                            expectedTotal = response.items.size
                        }

                        response.items.forEach { item ->
                            collectedItems.add(item)
                            currentCount++
                            onProgress(currentCount, expectedTotal, "Received sport mode session")
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
            binder.deleteSportModeHistory()
            
            // Wait for delete confirmation
            val deleteResponse = responseChannel.receive()
            if (deleteResponse is WatchHDeleteSportModeHistoryCommand.Response) {
                if (deleteResponse.status == 0.toByte()) {
                    Log.d(TAG, "Watch confirmed deletion of synced sport mode data")
                } else {
                    Log.w(TAG, "Watch delete confirmation failed with status: ${deleteResponse.status}")
                }
            }

            onProgress(expectedTotal, expectedTotal, "Sport mode sync completed successfully")
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