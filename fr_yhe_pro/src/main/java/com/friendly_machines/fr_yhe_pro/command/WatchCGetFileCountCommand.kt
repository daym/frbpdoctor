package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchCGetFileCountCommand : WatchCommand(WatchOperation.CGetFileCount, byteArrayOf(1)) {
    data class Response(val count: UByte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val count = buf.get().toUByte()
                return Response(count = count)
            }
        }
    }
}
