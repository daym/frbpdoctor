package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

// FIXME has a lot more parameters (5 total)
class WatchSSetTemperatureAlarmCommand(enabled: Boolean, temperature: UByte) : WatchCommand(
    WatchOperation.SSetTemperatureAlarm, byteArrayOf(
        when (enabled) {
            true -> 1
            false -> 0
        }, temperature.toByte(), 0
    )
) {
    data class Response(val status: Byte, val data: Byte?) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val status = buf.get()
                val data = if (buf.hasRemaining()) buf.get() else null
                return Response(status = status, data = data)
            }
        }
    }
}
