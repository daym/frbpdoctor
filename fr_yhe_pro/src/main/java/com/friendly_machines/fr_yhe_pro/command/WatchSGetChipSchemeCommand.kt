package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchSGetChipSchemeCommand : WatchCommand(WatchOperation.SGetChipScheme, ByteArray(0)) {
    data class Response(val scheme: Byte) : WatchResponse() { // scheme: example: 1
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val scheme = buf.get()
                return Response(scheme = scheme)
            }
        }
    }

}