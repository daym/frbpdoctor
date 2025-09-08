package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

// mode: 0 or 1
class WatchSSetDndModeCommand(mode: Byte, startTimeHour: Byte, startTimeMin: Byte, endTimeHour: Byte, endTimeMin: Byte) : WatchCommand(WatchOperation.SSetDnd, run {
    val buf = ByteBuffer.allocate(5)
    buf.put(mode)
    buf.put(startTimeHour)
    buf.put(startTimeMin)
    buf.put(endTimeHour)
    buf.put(endTimeMin)
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
