package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchAGetRealData(sensorType: Byte, measureType: Byte /* 1...5 */, duration: Byte): WatchCommand(WatchOperation.ARealData, byteArrayOf(sensorType, measureType, duration)) {

    // FIXME: Should be real-time data streaming triger command.  No status response.
    data class Response(val status: Byte = 0) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(status = 0)
            }
        }
    }
}