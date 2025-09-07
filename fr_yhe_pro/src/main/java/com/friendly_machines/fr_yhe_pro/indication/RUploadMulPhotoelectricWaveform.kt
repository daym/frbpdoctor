package com.friendly_machines.fr_yhe_pro.indication

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory.R_RESPONSE_CODE_OFFSET
import java.nio.ByteBuffer

data class PhotoelectricSample(val green: Int?, val ir: Int, val red: Int?)

// Note: Request should use WatchAGetRealData(sensorType = 15, measureType, duration)
// FIXME: Real-time response
data class RUploadMulPhotoelectricWaveform(
    val code: Short,
    val sampleType: Byte,
    val samples: List<PhotoelectricSample>
) : WatchResponse() {
    companion object {
        private const val WeCouldRespondCode: Short = (0x0F + R_RESPONSE_CODE_OFFSET).toShort()
        
        fun parse(buf: ByteBuffer): RUploadMulPhotoelectricWaveform {
            val sampleType = buf.get()
            val samples = mutableListOf<PhotoelectricSample>()
            
            when (sampleType.toInt() and 0xFF) {
                0 -> {
                    buf.order(java.nio.ByteOrder.LITTLE_ENDIAN)
                    while (buf.remaining() >= 3*4) {
                        val green = buf.int
                        val ir = buf.int  
                        val red = buf.int
                        samples.add(PhotoelectricSample(green, ir, red))
                    }
                }
                1 -> {
                    buf.order(java.nio.ByteOrder.BIG_ENDIAN)
                    while (buf.remaining() >= 2*4) { // green, ir
                        val green = buf.int
                        val ir = buf.int
                        samples.add(PhotoelectricSample(green, ir, null))
                    }
                }
                2 -> {
                    buf.order(java.nio.ByteOrder.BIG_ENDIAN)
                    while (buf.remaining() >= 8) { // ir, red
                        val ir = buf.int
                        val red = buf.int
                        samples.add(PhotoelectricSample(null, ir, red))
                    }
                }
            }
            
            return RUploadMulPhotoelectricWaveform(code = WeCouldRespondCode, sampleType = sampleType, samples = samples)
        }
    }
}