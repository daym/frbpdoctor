package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchCSyncDataCommand(data: ByteArray = byteArrayOf()) : WatchCommand(WatchOperation.CSyncData, data) { // FIXME: what
    data class Response(val syncData: ByteArray) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                if (buf.remaining() >= 2) {
                    buf.position(buf.position() + 2)
                }
                val remaining = ByteArray(buf.remaining())
                buf.get(remaining)
                return Response(syncData = remaining)
            }
        }
    }
}
