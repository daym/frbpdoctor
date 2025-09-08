package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

// SAlarm is used in multiple commands
// x and b can be 0 (and usually are).
class WatchSAddAlarmCommand(x: Byte, y: Byte, z: Byte, a: Byte, b: Byte) : WatchCommand(WatchOperation.SAlarm, byteArrayOf(1.toByte(), x, y, z, a, b)) {
    data class Response(val optType: Byte, val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val optType = buf.get()  // Should be 1 for add command
                val status = buf.get()
                return Response(optType, status)
            }
        }
    }
}