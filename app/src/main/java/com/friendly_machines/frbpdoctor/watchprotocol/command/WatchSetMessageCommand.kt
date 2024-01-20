package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchMessageEncodingException
import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchSetMessageCommand(type: Byte, time: Int, title: ByteArray, content: ByteArray) : WatchCommand(WatchOperation.SetMessage, run {
//    val title = ByteArray(0) // FIXME
//    val content = ByteArray(0) // FIXME
//    val type = 0.toByte() // FIXME 1 didn't work
    if (title.size > 120) {
        throw WatchMessageEncodingException("WatchSetMessageCommand title too long")
    }
    if (content.size > 120) {
        throw WatchMessageEncodingException("WatchSetMessageCommand content too long")
    }
    val buf = ByteBuffer.allocate(1 + 4 + 2 + title.size + 2 + content.size).order(ByteOrder.BIG_ENDIAN)
    buf.put(type)
    buf.putInt(time)
    buf.putShort(title.size.toShort())
    buf.put(title)
    buf.putShort(content.size.toShort())
    buf.put(content)
    buf.array()
})