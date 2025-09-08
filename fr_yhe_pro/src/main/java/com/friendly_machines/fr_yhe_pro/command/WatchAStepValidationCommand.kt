package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

// UNUSED
class WatchAStepValidationCommand(stepCount: Int, validationMode: Byte) : WatchCommand(WatchOperation.AStepValidation, run {
    val buf = ByteBuffer.allocate(5).order(ByteOrder.LITTLE_ENDIAN)
    buf.putInt(stepCount)
    buf.put(validationMode)
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
