package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchASetProductInfoCommand(typeCode: Byte, productInfo: String) : WatchCommand(WatchOperation.ASetProductInfo, run {
    val infoBytes = productInfo.toByteArray(Charsets.UTF_8)
    val totalSize = 1 + infoBytes.size + 1
    val buffer = ByteBuffer.allocate(totalSize).apply {
        order(ByteOrder.LITTLE_ENDIAN)
        put(typeCode)
        put(infoBytes)
        put(0)
    }
    buffer.array()
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