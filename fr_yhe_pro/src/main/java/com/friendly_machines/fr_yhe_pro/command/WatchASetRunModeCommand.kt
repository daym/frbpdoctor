package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchASetRunModeCommand(key: Byte, value: Byte) : WatchCommand(WatchOperation.ASetRunMode, byteArrayOf(key, value)) {
    
    data class Response(val status: Byte = 0) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                // ignore
                return Response(status = 0)
            }
        }
    }
}