package com.friendly_machines.frbpdoctor.ui.customization

import android.util.Log
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchBinder
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchListener
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_med.WatchBigResponseMed
import com.friendly_machines.frbpdoctor.MedBigResponseBuffer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeoutOrNull

class AlarmController(val binder: IWatchBinder) : IWatchListener, MedBigResponseBuffer.IBigResponseListener {
    private val TAG: String = "AlarmController"
    private val bigResponseChannel = Channel<WatchBigResponseMed>()
    
    override fun onWatchResponse(response: WatchResponse) {
        // Handle regular alarm responses if needed (like addAlarm confirmations)
    }

    override fun onBigWatchResponse(response: WatchBigResponseMed) {
        // Handle big responses like GetAlarm data
        val result = bigResponseChannel.trySend(response)
        if (result.isFailure) {
            Log.e(TAG, "Failed to send ${response::class.simpleName}: ${result.exceptionOrNull()}")
            bigResponseChannel.close()
        }
    }

    override fun onException(exception: Throwable) {
        // Handle exceptions
    }

    suspend fun getAlarms(): Array<com.friendly_machines.fr_yhe_api.commondata.AlarmDataBlock>? {
        binder.getAlarm()
        // Wait for the WatchBigResponseMed.GetAlarm with 10 second timeout
        return withTimeoutOrNull(10000) {
            var result: Array<com.friendly_machines.fr_yhe_api.commondata.AlarmDataBlock>? = null
            while (result == null) {
                val response = bigResponseChannel.receive()
                if (response is WatchBigResponseMed.GetAlarm) {
                    result = response.data
                }
            }
            result
        }
    }
    
    suspend fun listAlarms(): Array<com.friendly_machines.fr_yhe_api.commondata.AlarmDataBlock>? {
        return getAlarms()
    }
    
    suspend fun deleteAlarm(x: Byte, y: Byte) {
        binder.deleteAlarm(x, y)
        // Note: Delete response handling would go here if needed
    }
}