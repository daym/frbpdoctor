package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_api.commondata.DayOfWeekPattern
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchSSetRegularReminderCommand(enabled: Byte, startHour: Byte, startMinute: Byte, endHour: Byte, endMinute: Byte, dayOfWeekPattern: Set<DayOfWeekPattern>, intervalInMinutes: Byte, message: String?): WatchCommand(WatchOperation.SSetRegularReminder, run {
    val messageUtf8 = message?.toByteArray(charset = Charsets.UTF_8)
    val messageUtf8Len = messageUtf8?.size ?: 0
    val buf = ByteBuffer.allocate(7 + messageUtf8Len).order(ByteOrder.LITTLE_ENDIAN)
    buf.put(enabled)
    buf.put(startHour)
    buf.put(startMinute)
    buf.put(endHour)
    buf.put(endMinute)
    buf.put(DayOfWeekPattern.toByte(dayOfWeekPattern))
    buf.put(intervalInMinutes)
    messageUtf8?.let {
        buf.put(it)
    }

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