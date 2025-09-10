package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

/**
 * Anti-loss (disconnection) monitoring command.
 * @param type Anti-loss type: 0=disabled, 2=enabled
 */
class WatchSSetAntiLossCommand(type: Byte) : WatchCommand(WatchOperation.SSetAntiLoss, byteArrayOf(type)) {
    data class Response(val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val status = buf.get()
                return Response(status = status)
            }
        }
    }
}