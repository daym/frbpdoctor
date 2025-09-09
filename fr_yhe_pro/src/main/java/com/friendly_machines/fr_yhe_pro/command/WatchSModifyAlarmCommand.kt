package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

// SAlarm is used in multiple commands
class WatchSModifyAlarmCommand(oldHour: Byte, oldMinute: Byte, enabled: Boolean, newHour: Byte, newMinute: Byte, weekPattern: Byte, reserved: Byte = 0) : WatchCommand(
    WatchOperation.SAlarm, byteArrayOf(
        3.toByte(), oldHour, oldMinute, if (enabled) {
            1
        } else {
            0
        }, newHour, newMinute, weekPattern, reserved
    )
) {
    data class Response(val optType: Byte, val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val optType = buf.get()  // Should be 3 for modify command
                val status = buf.get()
                return Response(optType, status)
            }
        }
    }
}