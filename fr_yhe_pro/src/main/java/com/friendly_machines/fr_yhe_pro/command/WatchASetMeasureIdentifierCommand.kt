package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.commondata.TimeUtils
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchASetMeasureIdentifierCommand(measureType: Byte, timestamp: Long, param1: Byte, param2: Byte, param3: Byte, param4: Byte, param5: Byte, param6: Byte) : WatchCommand(WatchOperation.ASetMeasureIdentifier, run {
    val timezoneOffset = java.util.TimeZone.getDefault().getOffset(System.currentTimeMillis()).toLong()
    val offset = TimeUtils.localUnixMillisToWatchTime(timestamp, timezoneOffset)
    val buf = ByteBuffer.allocate(11).order(ByteOrder.LITTLE_ENDIAN)
    buf.put(measureType)
    buf.putInt(offset.toInt())
    buf.put(param1)
    buf.put(param2)
    buf.put(param3)
    buf.put(param4)
    buf.put(param5)
    buf.put(param6)
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