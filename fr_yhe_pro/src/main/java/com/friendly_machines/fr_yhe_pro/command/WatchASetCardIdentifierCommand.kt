package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchASetCardIdentifierCommand(typeCode: Byte, cardId: String) : WatchCommand(WatchOperation.ASetCardIdentifier, run {
    val cardBytes = cardId.toByteArray(Charsets.UTF_8)
    val data = ByteArray(1 + cardBytes.size)
    data[0] = typeCode
    cardBytes.copyInto(data, 1) // FIXLME: 0 or not?
    data
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
