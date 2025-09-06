package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchRGetSportCommand : WatchCommand(WatchOperation.RSport, byteArrayOf()) {
    // FIXME: Real-time response
    data class Response(val step: Short, val distance: Short, val calorie: Short) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(
                    step = buf.short,
                    distance = buf.short,
                    calorie = buf.short
                )
            }
        }
    }
}
