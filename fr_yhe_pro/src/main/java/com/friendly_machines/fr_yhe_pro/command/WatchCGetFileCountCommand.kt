package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchCGetFileCountCommand : WatchCommand(WatchOperation.CGetFileCount, byteArrayOf(1)) {
    data class Response(val count: Short) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                buf.order(ByteOrder.LITTLE_ENDIAN)
                val count = if (buf.remaining() >= 2) buf.short else 0
                return Response(count = count)
            }
        }
    }
}
