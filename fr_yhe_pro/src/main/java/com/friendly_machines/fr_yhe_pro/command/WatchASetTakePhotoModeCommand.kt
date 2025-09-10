package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

// Make watch go into "take photo" UI.
class WatchASetTakePhotoModeCommand(
    action: Byte  // 0 (stop camera mode), 1 (start camera mode)
) : WatchCommand(WatchOperation.ATakePhotoMode, run {
    val buf = ByteBuffer.allocate(1).order(ByteOrder.LITTLE_ENDIAN)
    buf.put(action)
    buf.array()
}) {
    data class Response(val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                // A* commands read LAST byte as status
                val lastPos = buf.limit() - 1
                buf.position(lastPos)
                val status = buf.get()
                return Response(status = status)
            }
        }
    }
}