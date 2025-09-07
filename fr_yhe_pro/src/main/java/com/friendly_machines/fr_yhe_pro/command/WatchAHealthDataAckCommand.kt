package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchAHealthDataAckCommand(ackCode: Byte, message: String) : WatchCommand(WatchOperation.AHealthDataAck, run {
    val messageBytes = message.toByteArray(Charsets.UTF_8)
    val totalSize = 1 + messageBytes.size
    val buffer = ByteBuffer.allocate(totalSize).apply {
        order(ByteOrder.LITTLE_ENDIAN)
        put(ackCode)
        put(messageBytes)
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
