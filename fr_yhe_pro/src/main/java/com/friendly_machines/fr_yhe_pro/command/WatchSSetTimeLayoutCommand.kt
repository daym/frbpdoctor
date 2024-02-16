package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchTimePosition
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

// only for chipScheme == 3
class WatchSSetTimeLayoutCommand(val position: WatchTimePosition, val rgb565Color: UShort) : WatchCommand(WatchOperation.SSetTimeLayout, run {
    val buf = ByteBuffer.allocate(7).order(ByteOrder.LITTLE_ENDIAN)
    buf.put(
        when (position) {
            WatchTimePosition.Top -> 1.toByte()
            WatchTimePosition.Bottom -> 2.toByte()
            WatchTimePosition.Left -> 3.toByte()
            WatchTimePosition.Right -> 4.toByte()
            WatchTimePosition.LeftTop -> 5.toByte()
            WatchTimePosition.RightTop -> 6.toByte()
            WatchTimePosition.LeftBottom -> 7.toByte()
            WatchTimePosition.RightBottom -> 8.toByte()
            WatchTimePosition.Middle -> 9.toByte()
        }
    )
    buf.putShort(rgb565Color.toShort()) // FIXME UShort
    buf.array()
}) {
    data class Response(val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(status = buf.get())
            }
        }
    }
}