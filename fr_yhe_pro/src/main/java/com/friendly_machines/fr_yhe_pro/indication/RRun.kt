package com.friendly_machines.fr_yhe_pro.indication

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory.R_RESPONSE_CODE_OFFSET
import java.nio.ByteBuffer

// Note: Request should use WatchAGetRealData(sensorType = 6, measureType, duration)
// FIXME: Real-time response
data class RRun(val code: Short, val data: ByteArray) : WatchResponse() {
    companion object {
        private const val WeCouldRespondCode: Short = (0x06 + R_RESPONSE_CODE_OFFSET).toShort()
        
        fun parse(buf: ByteBuffer): RRun {
            val b = ByteArray(buf.remaining())
            buf.get(b)
            return RRun(code = WeCouldRespondCode, data = b)
        }
    }
}