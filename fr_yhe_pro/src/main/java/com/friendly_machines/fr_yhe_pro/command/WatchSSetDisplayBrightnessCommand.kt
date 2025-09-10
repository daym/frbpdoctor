package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

/**
 * Display brightness command.
 * Controls the screen brightness level of the watch.
 *
 * @param level Brightness level (0=lowest, 1=middle, 2=high, 3=auto, 4=lower, 5=higher)
 */
class WatchSSetDisplayBrightnessCommand(level: Byte) : WatchCommand(WatchOperation.SDisplayBrightness, byteArrayOf(level)) {
    data class Response(val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val status = buf.get()
                return Response(status = status)
            }
        }
    }
}