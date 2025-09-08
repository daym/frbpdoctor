package com.friendly_machines.frbpdoctor.ui.health

// FIXME: handle timestamps correctly

import android.content.Context
import android.util.Log
import androidx.health.connect.client.records.HeartRateRecord
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.fr_yhe_api.commondata.HHistoryHeartRateDataBlock
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchBinder
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.command.WatchHDeleteHeartHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetHeartHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHHistoryBlockCommand
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.coroutineScope
import java.time.Instant
import kotlin.reflect.KClass

/**
 * Controller for collecting heart rate history data.
 */
class HeartRateHistoryController private constructor(
    binder: IWatchBinder,
    context: Context,
    recyclerView: RecyclerView?,
    onProgress: (current: Int, total: Int, message: String) -> Unit,
    onComplete: () -> Unit,
    onError: (error: Exception) -> Unit
) : HistoryController<HHistoryHeartRateDataBlock>(binder, context, recyclerView, onProgress, onComplete, onError) {

    companion object {
        private const val TAG = "HeartRateHistoryController"

        val RESPONSE_TYPES: Set<KClass<out WatchResponse>> = setOf(
            WatchHGetHeartHistoryCommand.Response::class,
            WatchHHistoryBlockCommand.Response::class,
            WatchHDeleteHeartHistoryCommand.Response::class
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
        ): HeartRateHistoryController? {
            if (!HistoryControllerRegistry.startOperation(RESPONSE_TYPES, MED_BIG_RESPONSE_TYPES)) {
                return null
            }

            return try {
                val result = HeartRateHistoryController(binder, context, recyclerView, onProgress, onComplete, onError)
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

    private fun updateFragmentData() {
        recyclerView?.post {
            val heartDataBlocks = collectedItems.map { hHeartItem ->
                HHistoryHeartRateDataBlock(
                    timestamp = hHeartItem.timestamp,
                    heartRateInBpm = hHeartItem.heartRateInBpm,
                )
            }.toTypedArray()

            val adapter = HeartRateAdapter(heartDataBlocks.sortedBy { it.timestamp })
            recyclerView.adapter = adapter
        }
    }

    private suspend fun insertIntoHealthConnect() {
        try {
            val records = collectedItems.map { item ->
                HeartRateRecord(
                    startTime = item.timestamp.toInstant(),
                    startZoneOffset = null, // FIXME
                    endTime = item.timestamp.toInstant(), // FIXME should be range
                    endZoneOffset = null, // FIXME
                    samples = listOf(
                        HeartRateRecord.Sample(
                            time = item.timestamp.toInstant(),
                            beatsPerMinute = item.heartRateInBpm.toLong(),
                        )
                    )
                )
            }

            healthClient.insertRecords(records)
            Log.d(TAG, "Inserted ${records.size} heart rate records into HealthConnect")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert heart rate records into HealthConnect", e)
        }
    }

    override suspend fun startCollection() = coroutineScope {
        try {
            onProgress(0, 0, "Starting heart rate collection...")
            collectedItems.clear()

            // Start the heart history command
            binder.getHeartHistoryData()

            var currentCount = 0
            var expectedTotal = 0

            while (true) {
                val response = responseChannel.receive()

                when (response) {
                    is WatchHGetHeartHistoryCommand.Response -> {
                        if (expectedTotal == 0) {
                            expectedTotal = response.items.size
                        }

                        response.items.forEach { item ->
                            collectedItems.add(item)
                            currentCount++
                            onProgress(currentCount, expectedTotal, "Received heart rate record")
                            updateFragmentData()
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
            binder.deleteHeartHistory()

            // Wait for delete confirmation
            val deleteResponse = responseChannel.receive()
            if (deleteResponse is WatchHDeleteHeartHistoryCommand.Response) {
                if (deleteResponse.status == 0.toByte()) {
                    Log.d(TAG, "Watch confirmed deletion of synced heart rate data")
                } else {
                    Log.w(TAG, "Watch delete confirmation failed with status: ${deleteResponse.status}")
                }
            }

            onProgress(expectedTotal, expectedTotal, "Heart rate sync completed successfully")
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