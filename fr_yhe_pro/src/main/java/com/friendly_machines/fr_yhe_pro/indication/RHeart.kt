package com.friendly_machines.fr_yhe_pro.indication

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory.R_RESPONSE_CODE_OFFSET
import java.nio.ByteBuffer

// Note: Request should use WatchAGetRealData(sensorType = 1, measureType, duration)
// FIXME: Real-time response
data class RHeart(val code: Short, val heart: Byte) : WatchResponse() {
    companion object {
        private const val WeCouldRespondCode: Short = (0x01 + R_RESPONSE_CODE_OFFSET).toShort()
        
        fun parse(buf: ByteBuffer): RHeart {
            return RHeart(code = WeCouldRespondCode, heart = buf.get())
        }
    }
}