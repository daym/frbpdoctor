package com.friendly_machines.fr_yhe_pro.indication

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory.R_RESPONSE_CODE_OFFSET
import com.friendly_machines.fr_yhe_pro.TimeUtils
import java.nio.ByteBuffer

// Note: Request should use WatchAGetRealData(sensorType = 11, measureType, duration)
// FIXME: Real-time response
data class RSchedule(
    val code: Short,
    val index: Byte,
    val enable: Byte,
    val incidentIndex: Byte,
    val incidentEnable: Byte,
    val incidentTime: Long,
    val incidentId: Byte,
    val incidentName: String?
) : WatchResponse() {
    companion object {
        private const val WeCouldRespondCode: Short = (0x0B + R_RESPONSE_CODE_OFFSET).toShort()
        
        fun parse(buf: ByteBuffer): RSchedule {
            val index = buf.get()
            val enable = buf.get()
            val incidentIndex = buf.get()
            val incidentEnable = buf.get()

            val rawTime = buf.int.toLong()
            val timezoneOffset = java.util.TimeZone.getDefault().getOffset(System.currentTimeMillis()).toLong()
            val incidentTime = TimeUtils.watchTimeToLocalUnixMillis(rawTime, timezoneOffset)
            
            val incidentId = buf.get()
            
            val incidentName = if (buf.hasRemaining()) {
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
                ""
            }
            
            return RSchedule(code = WeCouldRespondCode, index = index, enable = enable, incidentIndex = incidentIndex, incidentEnable = incidentEnable, incidentTime = incidentTime, incidentId = incidentId, incidentName = incidentName)
        }
    }
}