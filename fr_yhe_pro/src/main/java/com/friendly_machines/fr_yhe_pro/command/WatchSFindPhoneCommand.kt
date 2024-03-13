package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchSFindPhoneCommand(x: Byte): WatchCommand(WatchOperation.SFindPhone, byteArrayOf(x)) {
    data class Response(val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val status = buf.get() // FIXME
                return Response(status = status)
            }
        }
    }

}