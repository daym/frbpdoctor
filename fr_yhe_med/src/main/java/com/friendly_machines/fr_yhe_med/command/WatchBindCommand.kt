package com.friendly_machines.fr_yhe_med.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_med.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest

// TODO @Throws(NoSuchAlgorithmException::class)
class WatchBindCommand(userId: Long, key: ByteArray) : WatchCommand(WatchOperation.Bind, run {
    val keyDigest = MessageDigest.getInstance("MD5").digest(key)
    //assert(newKeyDigest.equals(keyDigest))
    val buf = ByteBuffer.allocate(key.size + keyDigest.size + 2 + 8).order(ByteOrder.BIG_ENDIAN)
    buf.put(key)
    buf.put(keyDigest)
    buf.putShort(0) // TODO
    buf.putLong(userId)
    buf.array()
}) {
    data class Response(val status: Byte) : // verified
        WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val status: Byte = buf.get()
                return Response(status = status)
            }
        }
    }
}