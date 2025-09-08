package com.friendly_machines.fr_yhe_pro.indication

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory.R_RESPONSE_CODE_OFFSET
import java.nio.ByteBuffer

// Note: Request should use WatchAGetRealData(sensorType = 9, measureType, duration)
// FIXME: Real-time response
data class RAmbientLight(val code: Short, val data: ByteArray) : WatchResponse() {
    companion object {
        private const val WeCouldRespondCode: Short = (0x09 + R_RESPONSE_CODE_OFFSET).toShort()
        
        fun parse(buf: ByteBuffer): RAmbientLight {
            val b = ByteArray(buf.remaining())
            buf.get(b)
            return RAmbientLight(code = WeCouldRespondCode, data = b)
        }
    }
}