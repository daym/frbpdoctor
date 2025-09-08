package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchCDeleteByTimestampCommand(x: Byte, timestamp: Long): WatchCommand(WatchOperation.CDeleteByTimestamp, run {
    val buf = ByteBuffer.allocate(1 + 4).order(ByteOrder.LITTLE_ENDIAN)
    buf.put(x)
    buf.putInt(timestamp.toInt())
    buf.array()
}) {
    // FIXME: Unclear.
    data class Response(val dummy: Unit = Unit) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response()
            }
        }
    }
}