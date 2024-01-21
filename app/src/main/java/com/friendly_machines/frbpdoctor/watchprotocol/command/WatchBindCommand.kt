package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation
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
})