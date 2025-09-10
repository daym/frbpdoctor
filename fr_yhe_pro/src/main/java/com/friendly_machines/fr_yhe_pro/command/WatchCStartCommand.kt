package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

// Data collection list request - gets list of available data on watch for syncing.
class WatchCStartCommand(
    dataType: Byte  // 0 = ECG data, 1 = PPG data, other values = history data type ID
) : WatchCommand(WatchOperation.CStart, run {
    val buf = ByteBuffer.allocate(1).order(ByteOrder.LITTLE_ENDIAN)
    buf.put(dataType)
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