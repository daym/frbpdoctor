package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchSSetSleepReminderCommand(a: Byte, b: Byte, c: Byte) : WatchCommand(WatchOperation.SSetSleepReminder, byteArrayOf(a, b, c)) {
    data class Response(val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(buf.get())
            }
        }
    }
}
