package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchRGetAmbientLightCommand : WatchCommand(WatchOperation.RAmbientLight, byteArrayOf()) {
    data class Response(val data: ByteArray) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val b = ByteArray(buf.remaining())
                buf.get(b)
                return Response(b)
            }
        }
    }
}
