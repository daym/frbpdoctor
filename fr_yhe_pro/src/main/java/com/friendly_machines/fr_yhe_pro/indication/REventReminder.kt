package com.friendly_machines.fr_yhe_pro.indication

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory.R_RESPONSE_CODE_OFFSET
import java.nio.ByteBuffer

// Note: Request should use WatchAGetRealData(sensorType = 12, measureType, duration)
// FIXME: Real-time response
data class REventReminder(
    val code: Short,
    val index: Byte,
    val enabled: Byte,
    val type: Byte,
    val hour: Byte,
    val min: Byte,
    val repeat: Byte,
    val interval: Byte,
    val incidentName: String?
) : WatchResponse() {
    companion object {
        private const val WeCouldRespondCode: Short = (0x0C + R_RESPONSE_CODE_OFFSET).toShort()
        
        fun parse(buf: ByteBuffer): REventReminder {
            val index = buf.get()
            val enabled = buf.get()
            val type = buf.get()
            val hour = buf.get()
            val min = buf.get()
            val repeat = buf.get()
            val interval = buf.get()
            
            val incidentName = if (type.toInt() == 1 && buf.hasRemaining()) {
                val nameBytes = ByteArray(buf.remaining())
                buf.get(nameBytes)
                // Find null terminator and strip it
                val nullIndex = nameBytes.indexOf(0)
                if (nullIndex >= 0) {
                    String(nameBytes, 0, nullIndex, Charsets.UTF_8)
                } else {
                    String(nameBytes, Charsets.UTF_8)
                }
            } else {
                null
            }
            
            return REventReminder(code = WeCouldRespondCode, index = index, enabled = enabled, type = type, hour = hour, min = min, repeat = repeat, interval = interval, incidentName = incidentName)
        }
    }
}