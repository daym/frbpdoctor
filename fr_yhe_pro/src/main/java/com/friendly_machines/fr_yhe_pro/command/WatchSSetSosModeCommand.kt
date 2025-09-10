package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

// mode: 0 = disabled, 1 = enabled
class WatchSSetSosModeCommand(mode: Byte) : WatchCommand(WatchOperation.SSetSosMode, run {
    val buf = ByteBuffer.allocate(1).order(ByteOrder.LITTLE_ENDIAN)
    buf.put(mode)
    buf.array()
}) {
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
