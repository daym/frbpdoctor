package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

// Data sync verification response - confirms receipt of data during collection sync.
class WatchCSyncCheckResultCommand(
    dataType: Byte,
    status: Byte     // 0=success, 2=error, 3=CRC fail, 4=stop requested
) : WatchCommand(WatchOperation.CSyncCheckResult, run {
    val buf = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN)
    buf.put(dataType)
    buf.put(status)
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
