package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** This apparently sets up a watch-side filter for messages we pushed to the watch.
 * Not sure what good that does. Why send the message to the watch in the first place? */
class WatchSSetNotificationCommand(enabled: Boolean, flags: Array<Boolean>/*len: 21*/): WatchCommand(WatchOperation.SNotification, run {
    val buf = ByteBuffer.allocate(11).order(ByteOrder.LITTLE_ENDIAN)
    buf.put(if (enabled) 1.toByte() else 0.toByte())
    // FIXME convert flags to bytes
    buf.array()
}) {
    data class Response(val dummy: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val status = buf.get() // FIXME
                return Response(dummy = status)
            }
        }
    }
}