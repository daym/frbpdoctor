package com.friendly_machines.fr_yhe_pro.indication

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory.R_RESPONSE_CODE_OFFSET
import java.nio.ByteBuffer

// Note: Request should use WatchAGetRealData(sensorType = 5, measureType, duration)
// FIXME: This is a real-time stream
data class REcg(val code: Short, val ecgWaveform: List<Int>, val filteredWaveform: List<Int>) : WatchResponse() {
    companion object {
        private const val WeCouldRespondCode: Short = (0x05 + R_RESPONSE_CODE_OFFSET).toShort()

        private fun processEcgData(data: ByteArray): Pair<List<Int>, List<Int>> {
            val ecgWaveform = mutableListOf<Int>()
            val filteredWaveform = mutableListOf<Int>()

            // Process in 3-byte chunks
            var i = 0
            while (i + 2 < data.size) {
                val byte1 = data[i].toInt() and 0xFF
                val byte2 = data[i + 1].toInt() and 0xFF
                val byte3 = data[i + 2].toInt() and 0xFF

                // For now, reconstruct 16-bit values from the bytes
                // FIXME
                val ecgValue = byte1 or (byte2 shl 8)
                ecgWaveform.add(ecgValue)

                // FIXME  DSP
                val filteredValue = if (ecgWaveform.size >= 3) {
                    // Simple moving average filter
                    val recent = ecgWaveform.takeLast(3)
                    recent.sum() / recent.size
                } else {
                    ecgValue
                }
                filteredWaveform.add(filteredValue)

                i += 3
            }

            return Pair(ecgWaveform, filteredWaveform)
        }

        fun parse(buf: ByteBuffer): REcg {
            val data = ByteArray(buf.remaining())
            buf.get(data)

            val (ecgWaveform, filteredWaveform) = processEcgData(data)
            return REcg(code = WeCouldRespondCode, ecgWaveform = ecgWaveform, filteredWaveform = filteredWaveform)
        }
    }
}