package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchSSetScheduleSwitchCommand(enabled: Boolean) : WatchCommand(WatchOperation.SSetScheduleSwitch, byteArrayOf(if (enabled) 1.toByte() else 0.toByte())) {
    data class Response(val status: Byte): WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(status = buf.get())
            }
        }
    }
}