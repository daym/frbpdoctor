package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchSSetSleepReminderCommand(startHour: Byte, startMinute: Byte, daysOfWeekAndEnableAt0: UByte) : WatchCommand(WatchOperation.SSetSleepReminder, byteArrayOf(startHour, startMinute, daysOfWeekAndEnableAt0.toByte())) {
    data class Response(val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val status = buf.get() // can be -4
                return Response(status = status)
            }
        }
    }
}
