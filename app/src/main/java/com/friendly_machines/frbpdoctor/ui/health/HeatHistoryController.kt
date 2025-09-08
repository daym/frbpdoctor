package com.friendly_machines.frbpdoctor.ui.health

import android.content.Context
import android.util.Log
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.units.Temperature
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.fr_yhe_api.commondata.HTemperatureDataBlock
import com.friendly_machines.fr_yhe_api.commondata.HeatDataBlockMed
import com.friendly_machines.fr_yhe_api.commondata.Timestamp2000
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchBinder
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.command.WatchHGetTemperatureHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHHistoryBlockCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHDeleteTemperatureHistoryCommand
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.coroutineScope
import java.time.Instant
import kotlin.reflect.KClass

/**
 * Controller for collecting heat/temperature history data.
 */
class HeatHistoryController private constructor(
    binder: IWatchBinder,
    context: Context,
    recyclerView: RecyclerView?,
    onProgress: (current: Int, total: Int, message: String) -> Unit,
    onComplete: () -> Unit,
    onError: (error: Exception) -> Unit
) : HistoryController<HTemperatureDataBlock>(binder, context, recyclerView, onProgress, onComplete, onError) {

    companion object {
        private const val TAG = "HeatHistoryController"
        
        val RESPONSE_TYPES: Set<KClass<out WatchResponse>> = setOf(
            WatchHGetTemperatureHistoryCommand.Response::class,
            WatchHHistoryBlockCommand.Response::class,
            WatchHDeleteTemperatureHistoryCommand.Response::class
        )
        
        val MED_BIG_RESPONSE_TYPES: Set<KClass<out com.friendly_machines.fr_yhe_med.WatchBigResponseMed>> = setOf(
            com.friendly_machines.fr_yhe_med.WatchBigResponseMed.GetHeatData::class
        )

        suspend fun collect(
            binder: IWatchBinder,
            context: Context,
            recyclerView: RecyclerView?,
            onProgress: (current: Int, total: Int, message: String) -> Unit,
            onComplete: () -> Unit,
            onError: (error: Exception) -> Unit
        ): HeatHistoryController? {
            if (!HistoryControllerRegistry.startOperation(RESPONSE_TYPES, MED_BIG_RESPONSE_TYPES)) {
                return null
            }
            
            return try {
                val result = HeatHistoryController(binder, context, recyclerView, onProgress, onComplete, onError)
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
            val heatDataBlocks = collectedItems.map { item ->
                HeatDataBlockMed(
                    dayTimestamp = item.startTime.toUnixMillis().toUInt() / 1000U,
                    base = 0, // FIXME
                    sport = 0, // FIXME
                    walk = 0   // FIXME
                )
            }.toTypedArray()
            
            val adapter = HeatAdapter(heatDataBlocks.sortedBy { it.dayTimestamp })
            recyclerView.adapter = adapter
        }
    }

    private suspend fun insertIntoHealthConnect() {
        try {
            val records = collectedItems.map { item ->
                val tempCelsius = decodeIntegerDouble(item.valueInt.toUInt(), item.valueFloat.toUShort())
                BodyTemperatureRecord(
                    time = item.startTime.toInstant(),
                    zoneOffset = null, // FIXME
                    temperature = Temperature.celsius(tempCelsius)
                )
            }
            
            healthClient.insertRecords(records)
            Log.d(TAG, "Inserted ${records.size} temperature records into HealthConnect")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert temperature records into HealthConnect", e)
        }
    }

    private fun decodeIntegerDouble(valueInt: UInt, valueFloat: UShort): Double {
        // FIXME: Implement proper temperature decoding
        return valueInt.toDouble()
    }

    override suspend fun startCollection() = coroutineScope {
        try {
            onProgress(0, 0, "Starting temperature collection...")
            collectedItems.clear()
            
            binder.getHeatData()
            
            var currentCount = 0
            var expectedTotal = 0 // FIXME
            
            while (true) {
                val response = responseChannel.receive()
                
                when (response) {
                    is WatchHGetTemperatureHistoryCommand.Response -> {
                        if (expectedTotal == 0) {
                            expectedTotal = response.items.size
                        }
                        
                        response.items.forEach { item ->
                            collectedItems.add(item)
                            currentCount++
                            onProgress(currentCount, expectedTotal, "Received temperature record")
                            updateFragmentData()
                        }
                    }
                    is com.friendly_machines.fr_yhe_med.WatchBigResponseMed.GetHeatData -> {
                        response.data.forEach { heatData ->
                            val tempItem = HTemperatureDataBlock(
                                startTime = Timestamp2000.fromUnixMillis(heatData.dayTimestamp.toLong() * 1000L),
                                valueInt = heatData.base.toByte(), // FIXME: proper conversion
                                valueFloat = 0U.toByte(),
                                type = 0, // FIXME
                            )
                            collectedItems.add(tempItem)
                            currentCount++
                            onProgress(currentCount, response.data.size, "Received temperature record")
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
            
            insertIntoHealthConnect()
            
            // Send delete acknowledgment to watch (tells watch to delete synced data)
            onProgress(currentCount, expectedTotal, "Acknowledging data receipt to watch...")
            binder.deleteTemperatureHistory()
            
            // Wait for delete confirmation
            val deleteResponse = responseChannel.receive()
            if (deleteResponse is WatchHDeleteTemperatureHistoryCommand.Response) {
                if (deleteResponse.status == 0.toByte()) {
                    Log.d(TAG, "Watch confirmed deletion of synced temperature data")
                } else {
                    Log.w(TAG, "Watch delete confirmation failed with status: ${deleteResponse.status}")
                }
            }

            onProgress(expectedTotal, expectedTotal, "Temperature sync completed successfully")
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