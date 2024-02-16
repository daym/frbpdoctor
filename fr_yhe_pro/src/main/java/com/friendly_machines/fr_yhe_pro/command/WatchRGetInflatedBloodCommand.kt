package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

// FIXME more abstract type
class WatchRGetInflatedBloodCommand : WatchCommand(WatchOperation.RInflatedBlood, byteArrayOf()) {
    data class Response(val data: List<Pair<Short, Short>>) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val dataList = mutableListOf<Pair<Short, Short>>()
                while (buf.remaining() > 0) {
                    val pressure = buf.short
                    val signal = buf.short
                    dataList.add(pressure to signal)
                }
                return Response(dataList)
            }
        }
    }
}
