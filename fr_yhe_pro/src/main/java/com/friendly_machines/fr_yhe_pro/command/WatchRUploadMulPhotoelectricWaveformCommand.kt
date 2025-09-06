package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchRUploadMulPhotoelectricWaveformCommand : WatchCommand(WatchOperation.RUploadMulPhotoelectricWaveform, byteArrayOf()) {
    data class PhotoelectricSample(val green: Int?, val ir: Int?, val red: Int?)
    
    data class Response(
        val sampleType: Byte,
        val samples: List<PhotoelectricSample>
    ) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val sampleType = buf.get()
                val samples = mutableListOf<PhotoelectricSample>()
                
                when (sampleType.toInt() and 0xFF) {
                    0 -> {
                        while (buf.remaining() >= 3*4) {
                            val green = buf.int
                            val ir = buf.int  
                            val red = buf.int
                            samples.add(PhotoelectricSample(green, ir, red))
                        }
                    }
                    1 -> {
                        while (buf.remaining() >= 2*4) { // green, ir
                            val green = buf.int // FIXME: endian
                            val ir = buf.int // FIXME: endian
                            samples.add(PhotoelectricSample(green, ir, null))
                        }
                    }
                    2 -> {
                        while (buf.remaining() >= 8) { // ir, red
                            val ir = buf.int // FIXME: endian
                            val red = buf.int // FIXME: endian
                            samples.add(PhotoelectricSample(null, ir, red))
                        }
                    }
                }
                
                return Response(sampleType, samples)
            }
        }
    }
}