package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchGGetDeviceInfoCommand : WatchCommand(WatchOperation.GGetDeviceInfo, "GC".toByteArray(Charsets.US_ASCII)) {
    data class Response(val info: ByteArray) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val b = ByteArray(buf.remaining())
                buf.get(b)
                return Response(b)
            }
        }
    }
}
