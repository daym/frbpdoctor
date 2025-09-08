package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchGGetChipSchemeCommand : WatchCommand(WatchOperation.GGetChipScheme, ByteArray(0)) {
    data class Response(val scheme: Byte) : WatchResponse() { // scheme: example: 1
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val rawScheme = buf.get()
                val scheme = if ((rawScheme.toInt() and 0xFF) >= 240) 0.toByte() else rawScheme // TODO: check
                return Response(scheme = scheme)
            }
        }
    }
}
