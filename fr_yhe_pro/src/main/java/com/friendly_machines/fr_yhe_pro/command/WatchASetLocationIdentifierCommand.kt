package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchASetLocationIdentifierCommand(typeCode: Byte, locationId: String) : WatchCommand(WatchOperation.ASetLocationIdentifier, run {
        val locationBytes = locationId.toByteArray(Charsets.UTF_8)
        val totalSize = 1 + locationBytes.size
        val buffer = ByteBuffer.allocate(totalSize).apply {
            order(ByteOrder.LITTLE_ENDIAN)
            put(typeCode)
            put(locationBytes) // TODO: 0 terminate or not ?
        }
        buffer.array()
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