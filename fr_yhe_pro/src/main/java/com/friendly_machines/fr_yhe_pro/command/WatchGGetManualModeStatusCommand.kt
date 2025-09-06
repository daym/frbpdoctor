package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchGGetManualModeStatusCommand : WatchCommand(WatchOperation.GGetManualModeStatus, ByteArray(0)) {
    data class Response(val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val status = buf.get() // mode status, not regular status?
                return Response(status = status)
            }
        }
    }
}