package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchCommand
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest

// TODO @Throws(NoSuchAlgorithmException::class)
class BindCommand(userId: Long, key: ByteArray) : WatchCommand(17, run {
    val newKeyDigest = MessageDigest.getInstance("MD5").digest(key)
    //assert(newKeyDigest.equals(keyDigest))
    val buf = ByteBuffer.allocate(key.size + newKeyDigest.size + 2 + 8).order(ByteOrder.BIG_ENDIAN)
    buf.put(key)
    buf.put(newKeyDigest)
    buf.putShort(0) // TODO
    buf.putLong(userId)
    buf.array()
})