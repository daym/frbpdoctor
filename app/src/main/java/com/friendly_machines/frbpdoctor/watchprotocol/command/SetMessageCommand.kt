package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchCommand
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SetMessageCommand(type: Byte, time: Int, title: ByteArray, content: ByteArray) : WatchCommand(64, run {
    assert(title.size <= 120)
    assert(content.size <= 120)
    val buf = ByteBuffer.allocate(1 + 4 + 2 + title.size + 2 + content.size).order(ByteOrder.BIG_ENDIAN)
    buf.put(type)
    buf.putInt(time)
    buf.putShort(title.size.toShort())
    buf.put(title)
    buf.putShort(content.size.toShort())
    buf.put(content)
    buf.array()
})