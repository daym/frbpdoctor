package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchASyncMenstrualDataCommand(timestamp: Long, cycleDay: Byte, flowLevel: Byte) : WatchCommand(WatchOperation.ASyncMenstrualData, run {
    val offset = timestamp - 946684800L // Convert to watch epoch // FIXME: time constant; not here
    val buf = ByteBuffer.allocate(11).order(ByteOrder.LITTLE_ENDIAN)
    buf.putInt(offset.toInt())
    buf.put(cycleDay)
    buf.put(flowLevel)
    // Rest remain 0
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