package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

// "Find My Watch" feature - makes watch vibrate/beep so user can locate it.
class WatchAFindDeviceCommand(
    param1: Byte = 1,
    param2: Byte = 5,
    param3: Byte = 2
) : WatchCommand(WatchOperation.AFindDevice, run {
    val buf = ByteBuffer.allocate(3).order(ByteOrder.LITTLE_ENDIAN)
    buf.put(param1)
    buf.put(param2)
    buf.put(param3)
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