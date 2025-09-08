package com.friendly_machines.frbpdoctor.ui.health

import android.content.Context
import android.util.Log
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.units.Percentage
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.fr_yhe_api.commondata.HBloodOxygenDataBlock
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchBinder
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.command.WatchHGetBloodOxygenHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHHistoryBlockCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHDeleteBloodOxygenHistoryCommand
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.coroutineScope
import java.time.Instant
import kotlin.reflect.KClass

/**
 * Controller for collecting blood oxygen (SpO2) history data.
 */
class BloodOxygenHistoryController private constructor(
    binder: IWatchBinder,
    context: Context,
    recyclerView: RecyclerView?,
    onProgress: (current: Int, total: Int, message: String) -> Unit,
    onComplete: () -> Unit,
    onError: (error: Exception) -> Unit
) : HistoryController<HBloodOxygenDataBlock>(binder, context, recyclerView, onProgress, onComplete, onError) {

    companion object {
        private const val TAG = "BloodOxygenHistoryController"
        
        val RESPONSE_TYPES: Set<KClass<out WatchResponse>> = setOf(
            WatchHGetBloodOxygenHistoryCommand.Response::class,
            WatchHHistoryBlockCommand.Response::class,
            WatchHDeleteBloodOxygenHistoryCommand.Response::class
        )
        
        val MED_BIG_RESPONSE_TYPES: Set<KClass<out com.friendly_machines.fr_yhe_med.WatchBigResponseMed>> = setOf()

        suspend fun collect(
            binder: IWatchBinder,
            context: Context,
            recyclerView: RecyclerView?,
            onProgress: (current: Int, total: Int, message: String) -> Unit,
            onComplete: () -> Unit,
            onError: (error: Exception) -> Unit
        ): BloodOxygenHistoryController? {
            if (!HistoryControllerRegistry.startOperation(RESPONSE_TYPES, MED_BIG_RESPONSE_TYPES)) {
                return null
            }
            
            return try {
                val result = BloodOxygenHistoryController(binder, context, recyclerView, onProgress, onComplete, onError)
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

    private suspend fun insertIntoHealthConnect() {
        try {
            val records = collectedItems.map { item ->
                OxygenSaturationRecord(
                    time = item.startTime.toInstant(),
                    zoneOffset = null,
                    percentage = Percentage(item.bloodOxygen.toDouble())
                )
            }
            
            healthClient.insertRecords(records)
            Log.d(TAG, "Inserted ${records.size} blood oxygen records into HealthConnect")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert blood oxygen records into HealthConnect", e)
        }
    }

    override suspend fun startCollection() = coroutineScope {
        try {
            onProgress(0, 0, "Starting blood oxygen collection...")
            collectedItems.clear()

            // Start the blood oxygen history command
            binder.getBloodOxygenHistoryData()

            var currentCount = 0
            var expectedTotal = 0

            while (true) {
                val response = responseChannel.receive()

                when (response) {
                    is WatchHGetBloodOxygenHistoryCommand.Response -> {
                        if (expectedTotal == 0) {
                            expectedTotal = response.items.size
                        }

                        response.items.forEach { item ->
                            collectedItems.add(item)
                            currentCount++
                            onProgress(currentCount, expectedTotal, "Received SpO2 record")
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

            // Insert into HealthConnect first
            insertIntoHealthConnect()

            // Send delete acknowledgment to watch
            onProgress(currentCount, expectedTotal, "Acknowledging data receipt to watch...")
            binder.deleteBloodOxygenHistory()
            
            // Wait for delete confirmation
            val deleteResponse = responseChannel.receive()
            if (deleteResponse is WatchHDeleteBloodOxygenHistoryCommand.Response) {
                if (deleteResponse.status == 0.toByte()) {
                    Log.d(TAG, "Watch confirmed deletion of synced blood oxygen data")
                } else {
                    Log.w(TAG, "Watch delete confirmation failed with status: ${deleteResponse.status}")
                }
            }

            onProgress(expectedTotal, expectedTotal, "Blood oxygen sync completed successfully")
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