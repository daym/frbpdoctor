package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.commondata.PushMessageType
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

/**
 * Push message command to watch.
 * 
 * @param pushMessageType Push message type that determines the notification type and handling behavior
 * @param message Message text content to send to watch (UTF-8 encoded)
 *                Units: UTF-8 string, no length limit specified
 */
class WatchAPushMessageCommand(pushMessageType: PushMessageType, message: String): WatchCommand(WatchOperation.APushMessage, run {
    val messageBytes = message.toByteArray(Charsets.UTF_8)
    var buf = ByteBuffer.allocate(1 + messageBytes.size)
    buf.put(pushMessageType.value)
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
