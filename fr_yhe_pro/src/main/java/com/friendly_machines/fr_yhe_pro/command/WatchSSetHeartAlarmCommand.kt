package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchSSetHeartAlarmCommand(val enabled: Byte, val minValue: Byte, val maxValue: Byte) : WatchCommand(WatchOperation.SHeartAlarm, byteArrayOf(enabled, minValue, maxValue)) {
    data class Response(val status: Byte, val data: Byte?) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val status = buf.get() // FIXME
                val data = if (buf.hasRemaining()) buf.get() else null
                return Response(status = status, data = data)
            }
        }
    }
}