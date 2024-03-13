package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

/*
val index: Byte,
        val switch: Byte,
        val type: Byte,
        val hour: Byte,
        val min: Byte,
        val repeat: Byte,
        val interval: Byte
 */
class WatchSSetEventReminderCommand(a0: Byte, a1: Byte, a2: Byte, a3: Byte /* should be 1 for message to work */, a4: Byte, a5: Byte, a6: Byte, a7: Byte, message: String?): WatchCommand(WatchOperation.SSetEventReminder, run {
    val messageUtf8 = message?.toByteArray(charset = Charsets.UTF_8)
    val messageUtf8Len = messageUtf8?.size ?: 0
    val buf = ByteBuffer.allocate(8 + messageUtf8Len).order(ByteOrder.LITTLE_ENDIAN)
    buf.put(a0)
    buf.put(a1)
    buf.put(a2)
    buf.put(a3)
    buf.put(a4)
    buf.put(a5)
    buf.put(a6)
    buf.put(a7)
    messageUtf8?.let {
        assert(it.size <= 12) // FIXME nicer error handling
        buf.put(it)
    }
    buf.array()
}) {
    data class Response(val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(status = buf.get())
            }
        }
    }
}