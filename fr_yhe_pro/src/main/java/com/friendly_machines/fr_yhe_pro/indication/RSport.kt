package com.friendly_machines.fr_yhe_pro.indication

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory.R_RESPONSE_CODE_OFFSET
import java.nio.ByteBuffer

// Note: Request should use WatchAGetRealData(sensorType, measureType = 0, duration = 2)
// FIXME: Real-time response
data class RSport(val code: Short, val step: Short, val distance: Short, val calorie: Short) : WatchResponse() {
    companion object {
        private const val WeCouldRespondCode: Short = (0x00 + R_RESPONSE_CODE_OFFSET).toShort()
        
        fun parse(buf: ByteBuffer): RSport {
            return RSport(
                code = WeCouldRespondCode,
                step = buf.short,
                distance = buf.short,
                calorie = buf.short
            )
        }
    }
}