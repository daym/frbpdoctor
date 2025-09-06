package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchAGetRealData(x: Byte, i: Byte /* 1...5 */, z: Byte): WatchCommand(WatchOperation.ARealData, byteArrayOf(x, i, z)) {

    // FIXME: Should be real-time data streaming triger command.  No status response.
    data class Response(val status: Byte = 0) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(status = 0)
            }
        }
    }
}