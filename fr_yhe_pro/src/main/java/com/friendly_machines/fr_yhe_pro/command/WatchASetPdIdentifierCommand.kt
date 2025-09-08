package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchASetPdIdentifierCommand(id1: String, id2: String, typeCode: Byte, dataList: List<Map<String, Int>>) : WatchCommand(WatchOperation.ASetPDIdentifier, run {
    val id1Bytes = id1.toByteArray(Charsets.UTF_8)
    val id2Bytes = id2.toByteArray(Charsets.UTF_8)
    val data = ByteArray(id1Bytes.size + id2Bytes.size + 10) // Approximate size
    var pos = 0
    id1Bytes.copyInto(data, pos)
    pos += id1Bytes.size
    id2Bytes.copyInto(data, pos)
    pos += id2Bytes.size
    data[pos++] = typeCode
    // Add simplified data list representation; FIXME
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