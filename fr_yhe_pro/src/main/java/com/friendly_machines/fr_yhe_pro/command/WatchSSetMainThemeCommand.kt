package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchSSetMainThemeCommand(theme: Byte) : WatchCommand(WatchOperation.SSetMainTheme, byteArrayOf(theme)) {
    data class Response(val status: Byte) : WatchResponse() { // status == 0 ok
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(status = buf.get())
            }
        }
    }
}