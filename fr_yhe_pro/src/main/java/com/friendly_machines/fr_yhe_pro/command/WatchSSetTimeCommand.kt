package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchSSetTimeCommand(year: Short, month: Byte, day: Byte, hour: Byte, minute: Byte, second: Byte, weekDay: Byte) : WatchCommand(WatchOperation.SSetTime, run {
    val buf = ByteBuffer.allocate(2 + 1 + 1 + 1 + 1 + 1 + 1).order(ByteOrder.LITTLE_ENDIAN)
    buf.putShort(year)
    buf.put(month)
    buf.put(day)
    buf.put(hour)
    buf.put(minute)
    buf.put(second)
    buf.put(weekDay)
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