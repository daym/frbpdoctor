package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchWSetCurrentWatchDialCommand(val id: Int) : WatchCommand(WatchOperation.WSetCurrentWatchDial, run {
    val buf = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
    buf.putInt(id)
    buf.array()
}) {

    data class Response(val status: Byte) : WatchResponse() { // status == 0 ok
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(status = buf.get())
            }
        }
    }
}