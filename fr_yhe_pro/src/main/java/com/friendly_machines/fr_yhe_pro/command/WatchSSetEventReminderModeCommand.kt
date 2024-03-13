package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchSSetEventReminderModeCommand(enabled: Boolean): WatchCommand(WatchOperation.SSetEventReminderMode, byteArrayOf(if (enabled) 1 else 0)) {
    data class Response(val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val status = buf.get() // FIXME
                return Response(status = status)
            }
        }
    }
}