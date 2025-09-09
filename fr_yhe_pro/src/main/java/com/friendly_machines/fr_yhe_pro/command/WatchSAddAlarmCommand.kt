package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

// SAlarm is used in multiple commands
class WatchSAddAlarmCommand(alarmId: Byte/*=0*/, hour: Byte, minute: Byte, weekPattern: Byte, enabled: Boolean) : WatchCommand(WatchOperation.SAlarm, byteArrayOf(1.toByte(), alarmId, hour, minute, weekPattern, if (enabled) { 1 } else { 0 })) {
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