package com.friendly_machines.fr_yhe_pro.indication

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory.R_RESPONSE_CODE_OFFSET
import java.nio.ByteBuffer

// Note: Request should use WatchAGetRealData(sensorType = 7, measureType, duration)
// FIXME: Real-time response
data class RRespiration(val code: Short, val respirationRate: Byte) : WatchResponse() {
    companion object {
        private const val WeCouldRespondCode: Short = (0x07 + R_RESPONSE_CODE_OFFSET).toShort()
        
        fun parse(buf: ByteBuffer): RRespiration {
            return RRespiration(code = WeCouldRespondCode, respirationRate = buf.get())
        }
    }
}