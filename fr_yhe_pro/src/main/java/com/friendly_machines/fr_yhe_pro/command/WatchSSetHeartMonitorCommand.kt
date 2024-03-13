package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchSSetHeartMonitorCommand(val type: Byte, val interval: Byte): WatchCommand(WatchOperation.SHeartMonitor, byteArrayOf(type, interval)) {
    data class Response(val dummy: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val status = buf.get() // FIXME
                return Response(dummy = status)
            }
        }
    }
}