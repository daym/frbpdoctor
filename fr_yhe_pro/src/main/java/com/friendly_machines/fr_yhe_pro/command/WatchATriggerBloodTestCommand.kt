package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchATriggerBloodTestCommand(testType: Byte, param1: Byte, param2: Byte, param3: Byte, param4: Byte, param5: Byte, param6: Byte, param7: Byte) : WatchCommand(WatchOperation.ATriggerBloodTest, run { // FIXME: WHAT THE FUCK ARE THOSE PARAMETERS
    val buf = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)
    buf.put(testType)
    buf.put(param1)
    buf.put(param2)
    buf.put(param3)
    buf.put(param4)
    buf.put(param5)
    buf.put(param6)
    buf.put(param7)
    buf.array()
}) {
    data class Response(val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val status = if (buf.hasRemaining()) {
                    buf.position(buf.limit() - 1)
                    buf.get()
                } else 0
                return Response(status = status)
            }
        }
    }
}
