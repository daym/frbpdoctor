package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchAHealthConfigCommand(healthMode: Byte, intervalMinutes: Byte, alertThreshold: Byte, monitoringFlags: Byte) : WatchCommand(WatchOperation.AHealthConfig, run {
    val buf = ByteBuffer.allocate(14).order(ByteOrder.LITTLE_ENDIAN)
    buf.put(healthMode)
    buf.put(intervalMinutes)
    buf.put(alertThreshold)
    buf.put(monitoringFlags)
    buf.array()
}) {
    data class Response(val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val status = if (buf.hasRemaining()) {
                    buf.position(buf.limit() - 1)
                    buf.get()
                } else 0
                return Response(status = status)
            }
        }
    }
}
