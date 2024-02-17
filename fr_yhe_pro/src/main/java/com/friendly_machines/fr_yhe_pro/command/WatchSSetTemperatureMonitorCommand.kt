package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

// interval is a multiple of 10
class WatchSSetTemperatureMonitorCommand(enable: Boolean, interval: Byte) : WatchCommand(
    WatchOperation.SSetTemperatureMonitor, byteArrayOf(
        when (enable) {
            true -> 1
            false -> 0
        }, interval
    )
) {
    data class Response(val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(status = buf.get())
            }
        }
    }
}