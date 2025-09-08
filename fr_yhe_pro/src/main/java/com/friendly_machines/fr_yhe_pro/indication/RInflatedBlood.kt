package com.friendly_machines.fr_yhe_pro.indication

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory.R_RESPONSE_CODE_OFFSET
import java.nio.ByteBuffer

// Note: Request should use WatchAGetRealData(sensorType = 14, measureType, duration)
// FIXME: Real-time response
data class RInflatedBlood(val code: Short, val pressureSignalPairs: List<Pair<Short, Short>>) : WatchResponse() {
    companion object {
        private const val WeCouldRespondCode: Short = (0x0E + R_RESPONSE_CODE_OFFSET).toShort()
        
        fun parse(buf: ByteBuffer): RInflatedBlood {
            buf.order(java.nio.ByteOrder.LITTLE_ENDIAN)
            val pressureSignalPairs = mutableListOf<Pair<Short, Short>>()
            while (buf.remaining() >= 4) {
                val pressureValue = buf.short
                val signalValue = buf.short
                pressureSignalPairs.add(pressureValue to signalValue)
            }
            return RInflatedBlood(code = WeCouldRespondCode, pressureSignalPairs = pressureSignalPairs)
        }
    }
}