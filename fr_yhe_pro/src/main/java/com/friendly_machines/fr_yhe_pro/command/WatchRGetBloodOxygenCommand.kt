package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchRGetBloodOxygenCommand : WatchCommand(WatchOperation.RBloodOxygen, byteArrayOf()) {
    data class Response(val bloodOxygen: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(buf.get())
            }
        }
    }
}
