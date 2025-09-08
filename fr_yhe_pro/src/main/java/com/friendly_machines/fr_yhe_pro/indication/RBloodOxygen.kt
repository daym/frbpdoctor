package com.friendly_machines.fr_yhe_pro.indication

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory.R_RESPONSE_CODE_OFFSET
import java.nio.ByteBuffer

// Note: Request should use WatchAGetRealData(sensorType = 2, measureType, duration)
// FIXME: Real-time response
data class RBloodOxygen(val code: Short, val bloodOxygen: Byte) : WatchResponse() {
    companion object {
        private const val WeCouldRespondCode: Short = (0x02 + R_RESPONSE_CODE_OFFSET).toShort()
        
        fun parse(buf: ByteBuffer): RBloodOxygen {
            return RBloodOxygen(code = WeCouldRespondCode, bloodOxygen = buf.get())
        }
    }
}