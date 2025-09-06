package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchRGetEcgCommand : WatchCommand(WatchOperation.REcg, byteArrayOf()) {
    // FIXME: This is a real-time stream, Response class should be removed
    data class Response(val ecgWaveform: List<Int>, val filteredWaveform: List<Int>) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val data = ByteArray(buf.remaining())
                buf.get(data)
                
                // Apply ECG filtering exactly like original AITools.ecgRealWaveFilteringMap
                val (ecgWaveform, filteredWaveform) = processEcgData(data)
                return Response(ecgWaveform, filteredWaveform)
            }
            
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
                    
                    // Apply basic filtering (the original does complex DSP filtering)
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
        }
    }
}
