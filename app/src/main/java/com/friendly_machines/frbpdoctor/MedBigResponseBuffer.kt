package com.friendly_machines.frbpdoctor

import android.util.Log
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchListener
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchRawResponse
import com.friendly_machines.frbpdoctor.ui.health.HealthActivity
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MedBigResponseBuffer : IWatchListener {
    interface IBigResponseListener {
        fun onBigWatchResponse(response: com.friendly_machines.fr_yhe_med.WatchBigResponseMed) {

        }
    }

    private val bigBuffers = HashMap<Short, ByteArrayOutputStream>()
    var listener: IBigResponseListener? = null
    private fun bigAreWeDone(rawResponse: WatchRawResponse): Boolean {
        return rawResponse.arguments.isEmpty()
    }

    override fun onBigWatchRawResponse(rawResponse: WatchRawResponse) {
        val command = rawResponse.command
        // FIXME make sure the sequenceNumber are consecutive
        if (command == com.friendly_machines.fr_yhe_med.WatchOperation.DeviceInfo.code) {
            // TODO maybe handle the remainder here
            val buffer = bigBuffers[command]
            buffer?.let {
                bigBuffers[command] = ByteArrayOutputStream()
            }
        }
        if (bigAreWeDone(rawResponse)) {
            var buffer = bigBuffers[command]
            if (buffer == null) {
                buffer = ByteArrayOutputStream()
            }

            bigBuffers[command] = ByteArrayOutputStream()
            try {
                val response = com.friendly_machines.fr_yhe_med.WatchBigResponseMed.parse(
                    command, ByteBuffer.wrap(buffer.toByteArray()).order(
                        ByteOrder.BIG_ENDIAN
                    )
                )
                listener?.onBigWatchResponse(response)
            } catch (e: RuntimeException) {
                Log.d(HealthActivity.TAG, "Parse error while parsing ${buffer.toByteArray()}: $e")
            }
        } else {
            if (command == com.friendly_machines.fr_yhe_med.WatchBigResponseMed.RAW_BLOOD_PRESSURE) {
                if (rawResponse.arguments.size == 4 + 4 + 1 + 1 + 4) {
                    val buf = ByteBuffer.wrap(rawResponse.arguments).order(ByteOrder.BIG_ENDIAN)
                    if (buf.get() == 64.toByte() && buf.get() == 64.toByte() && buf.get() == 64.toByte() && buf.get() == 64.toByte()) {
                        val id = buf.int
                        val systolicPressure = buf.get()
                        val diastolicPressure = buf.get()
                        val time = buf.int
                        // TODO: also remember those?
                    }
                    return
                } // else adds it to buffer below
            }

            var buffer = bigBuffers[command]
            if (buffer == null) {
                buffer = ByteArrayOutputStream()
                bigBuffers[command] = buffer
            }
            buffer.write(rawResponse.arguments)
        }
    }

}