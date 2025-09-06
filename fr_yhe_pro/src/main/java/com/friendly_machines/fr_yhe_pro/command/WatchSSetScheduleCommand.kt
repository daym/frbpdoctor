package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchSSetScheduleCommand(a0: Byte, a1: Byte, a2: Byte, a3: Byte, a4: Byte, timestamp: Int, a5: Byte, message: String?): WatchCommand(WatchOperation.SSetSchedule, run {
    val messageUtf8 = message?.toByteArray(charset = Charsets.UTF_8)
    val messageUtf8Len = messageUtf8?.size ?: 0
    val buf = ByteBuffer.allocate(5 + 4 + 1 + messageUtf8Len).order(ByteOrder.LITTLE_ENDIAN)
    buf.put(a0)
    buf.put(a1)
    buf.put(a2)
    buf.put(a3)
    buf.put(a4)
    buf.putInt(timestamp)
    buf.put(a5)
    messageUtf8?.let {
        buf.put(it)
    }
    buf.array()
}) {
    data class Response(val status: Byte, val data: Byte?) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val status = buf.get() // can be -4
                val data = if (buf.hasRemaining()) buf.get() else null
                return Response(status = status, data = data)
            }
        }
    }
}