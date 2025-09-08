package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

// UNUSED
class WatchADataConfirmationCommand(confirmationType: Byte, sequenceId: Byte, status: Byte) : WatchCommand(WatchOperation.ADataConfirmation, run {
    val buf = ByteBuffer.allocate(6).order(ByteOrder.LITTLE_ENDIAN)
    buf.put(confirmationType)
    buf.put(sequenceId)
    buf.put(status)
    buf.array()
}) {
    data class Response(val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val bytes = ByteArray(buf.remaining())
                buf.get(bytes)
                val status = if (bytes.isNotEmpty()) bytes.last() else 0
                return Response(status = status)
            }
        }
    }
}
