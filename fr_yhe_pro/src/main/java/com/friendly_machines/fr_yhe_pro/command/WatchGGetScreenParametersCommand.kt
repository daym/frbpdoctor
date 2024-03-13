package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchGGetScreenParametersCommand : WatchCommand(WatchOperation.GGetScreenParameters, ByteArray(0)) {
    data class Response(val type: Byte, val width: Short, val height: Short, val corner: Short) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val type = buf.get() // FIXME if that's 252, nothing else seems to be sent
                val width = buf.short
                val height = buf.short
                val corner = buf.short
                if (buf.remaining() >= 6) {
                    val cpWidth = buf.short
                    val cpHeight = buf.short
                    val cpCorner = buf.short
                }
                return Response(type = type, width = width, height = height, corner = corner)
            }
        }
    }
}