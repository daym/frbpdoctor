package com.friendly_machines.fr_yhe_med.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_med.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchSetWatchFaceCommand(id: Int) : WatchCommand(WatchOperation.SetWatchFace, run {
    val buf = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN)
    buf.putInt(id)
    buf.array()
}) {
    data class Response(val status: Byte) : // verified
        WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val status: Byte = buf.get()
                return Response(status = status)
            }
        }
    }
}