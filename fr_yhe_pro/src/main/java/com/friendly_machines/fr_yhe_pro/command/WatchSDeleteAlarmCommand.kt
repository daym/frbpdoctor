package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

// SAlarm is used in multiple commands
class WatchSDeleteAlarmCommand(x: Byte, y: Byte) : WatchCommand(WatchOperation.SAlarm, byteArrayOf(2.toByte(), x, y)) {
    data class Response(val optType: Byte, val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val optType = buf.get()  // Should be 2 for delete command
                val status = buf.get()
                return Response(optType, status)
            }
        }
    }
}