package com.friendly_machines.fr_yhe_pro.indication

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory.R_RESPONSE_CODE_OFFSET
import java.nio.ByteBuffer

// Note: Request should use WatchAGetRealData(sensorType = 3, measureType, duration)
// FIXME: Real-time response
data class RBloodPressure(val code: Short, val systolicPressure: Byte, val diastolicPressure: Byte, val heartValue: Byte, val hrv: Byte?) : WatchResponse() {
    companion object {
        private const val WeCouldRespondCode: Short = (0x03 + R_RESPONSE_CODE_OFFSET).toShort()
        
        fun parse(buf: ByteBuffer): RBloodPressure {
            val systolic = buf.get()
            val diastolic = buf.get()  
            val heart = buf.get()
            val hrv = if (buf.hasRemaining()) buf.get() else null
            return RBloodPressure(
                code = WeCouldRespondCode,
                systolicPressure = systolic,
                diastolicPressure = diastolic,
                heartValue = heart,
                hrv = hrv
            )
        }
    }
}