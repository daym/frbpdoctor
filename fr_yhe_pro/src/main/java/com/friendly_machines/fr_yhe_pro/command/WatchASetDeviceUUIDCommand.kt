package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchASetDeviceUUIDCommand(uuid: String) : WatchCommand(WatchOperation.ASetDeviceUUID, run {
    // Parse hex UUID string to 16 bytes
    val cleanUuid = if (uuid.length == 36) uuid.replace("-", "") else uuid
    require(cleanUuid.length == 32) { "UUID must be 32 or 36 characters" }
    
    ByteArray(16) { i ->
        cleanUuid.substring(i * 2, i * 2 + 2).toInt(16).toByte()
    }
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