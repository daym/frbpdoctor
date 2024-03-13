package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchSSetRegularReminderCommand(startHour: Byte, startMinute: Byte, endHour: Byte, endMinute: Byte, weekPattern: UByte, intervalInMinutes: Byte/*max (inkl): 60*/, message: String?): WatchCommand(WatchOperation.SSetRegularReminder, run {
    val messageUtf8 = message?.toByteArray(charset = Charsets.UTF_8)
    val messageUtf8Len = messageUtf8?.size ?: 0
    val buf = ByteBuffer.allocate(7 + messageUtf8Len).order(ByteOrder.BIG_ENDIAN)
    buf.put(0)
    buf.put(startHour)
    buf.put(startMinute)
    buf.put(endHour)
    buf.put(endMinute)
    buf.put(255.toByte()) // FIXME
    buf.put(intervalInMinutes)
    messageUtf8?.let {
        buf.put(it)
    }

    buf.array()
}) {
    data class Response(val FIXME: Byte) : WatchResponse() { // FIXME -4 ?!
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(FIXME = buf.get()) // TODO
            }
        }
    }
}