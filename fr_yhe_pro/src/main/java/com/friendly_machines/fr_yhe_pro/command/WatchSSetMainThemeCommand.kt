package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchSSetMainThemeCommand(theme: Byte) : WatchCommand(WatchOperation.SSetMainTheme, byteArrayOf(theme)) {
    data class Response(val status: Byte, val data: Byte?) : WatchResponse() { // status == 0 ok
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val status = buf.get()
                val data = if (buf.hasRemaining()) buf.get() else null
                return Response(status = status, data = data)
            }
        }
    }
}