package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

/**
 * Raise-to-wake screen command.
 * Controls whether the watch screen turns on when user raises their wrist.
 *
 * @param enabled True to enable raise-to-wake, false to disable
 */
class WatchSSetRaiseScreenCommand(enabled: Boolean) : WatchCommand(WatchOperation.SRaiseScreen, byteArrayOf(if (enabled) 1 else 0)) {
    data class Response(val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val status = buf.get()
                return Response(status = status)
            }
        }
    }
}