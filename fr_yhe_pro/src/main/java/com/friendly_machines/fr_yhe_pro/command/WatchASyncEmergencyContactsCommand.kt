package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchASyncEmergencyContactsCommand(name: String, phone: String) : WatchCommand(WatchOperation.ASyncEmergencyContacts, run {
    val nameBytes = name.toByteArray(Charsets.UTF_8)
    val phoneBytes = phone.toByteArray(Charsets.UTF_8)
    val totalSize = 1 + 1 + 1 + phoneBytes.size + nameBytes.size
    val buffer = ByteBuffer.allocate(totalSize).apply {
        order(ByteOrder.LITTLE_ENDIAN)
        put(1) // Command prefix
        put(nameBytes.size.toByte())
        put(phoneBytes.size.toByte())
        put(nameBytes)
        put(phoneBytes)
    }
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
