package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchRGetSportCommand : WatchCommand(WatchOperation.RSport, byteArrayOf()) {
    data class Response(val step: Short, val calorie: Short, val distance: Short) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(
                    step = buf.short,
                    calorie = buf.short,
                    distance = buf.short
                )
            }
        }
    }
}
