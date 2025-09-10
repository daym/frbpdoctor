package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

/**
 * Controls the screen brightness level of the watch.
 *
 * @param level Brightness level:
 *              0 = Lowest brightness
 *              1 = Middle brightness (default)
 *              2 = High brightness
 *              3 = Auto brightness
 *              4 = Lower brightness  
 *              5 = Higher brightness
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