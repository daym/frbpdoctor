package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchASyncEmergencyContactsCommand(name: String, phone: String) : WatchCommand(WatchOperation.ASyncEmergencyContacts, run {
    val nameBytes = name.toByteArray(Charsets.UTF_8)
    val phoneBytes = phone.toByteArray(Charsets.UTF_8)
    val totalSize = 1 + nameBytes.size + 1 + phoneBytes.size
    val buffer = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN)
    buffer.put(nameBytes.size.toByte())
    buffer.put(nameBytes)
    buffer.put(phoneBytes.size.toByte())
    buffer.put(phoneBytes)
    buffer.array()
}) {
    data class Response(val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val status = if (buf.hasRemaining()) buf.get() else 0
                return Response(status = status)
            }
        }
    }
}
