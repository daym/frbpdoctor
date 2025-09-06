package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchAPushMessageCommand(x: Byte, message: String): WatchCommand(WatchOperation.APushMessage, run {
    val messageBytes = message.toByteArray(Charsets.UTF_8)
    var buf = ByteBuffer.allocate(1 + messageBytes.size)
    buf.put(x)
    buf.put(messageBytes)
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
