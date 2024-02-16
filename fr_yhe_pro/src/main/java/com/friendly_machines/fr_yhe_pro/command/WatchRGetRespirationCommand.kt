package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchRGetRespirationCommand : WatchCommand(WatchOperation.RRespiration, byteArrayOf()) {
    data class Response(val respirationRate: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(buf.get())
            }
        }
    }
}
