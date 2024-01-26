package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchMessageEncodingException
import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchSetMessageCommand(type: Byte, time: Int, title: ByteArray, content: ByteArray) : WatchCommand(WatchOperation.SetMessage, run {
    if (title.size > 120) {
        throw WatchMessageEncodingException("WatchSetMessageCommand title too long")
    }
    if (content.size > 120) {
        throw WatchMessageEncodingException("WatchSetMessageCommand content too long")
    }
    val buf = ByteBuffer.allocate(1 + 4 + 2 + title.size + 2 + content.size).order(ByteOrder.BIG_ENDIAN)
    buf.put(type)
    // I think for type == call, none of the other fields are necessary (title is used, though, if present)
    buf.putInt(time)
    buf.putShort(title.size.toShort())
    buf.put(title)
    buf.putShort(content.size.toShort())
    buf.put(content)
    buf.array()
})