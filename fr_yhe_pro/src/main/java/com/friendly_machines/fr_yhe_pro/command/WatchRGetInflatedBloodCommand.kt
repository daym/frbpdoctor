package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

// FIXME more abstract type
class WatchRGetInflatedBloodCommand : WatchCommand(WatchOperation.RInflatedBlood, byteArrayOf()) {
    data class Response(val pressureSignalPairs: List<Pair<Short, Short>>) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val pressureSignalPairs = mutableListOf<Pair<Short, Short>>()
                while (buf.remaining() >= 4) {
                    val pressureValue = buf.short
                    val signalValue = buf.short
                    pressureSignalPairs.add(pressureValue to signalValue)
                }
                return Response(pressureSignalPairs)
            }
        }
    }
}
