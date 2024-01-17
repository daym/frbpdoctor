package com.friendly_machines.frbpdoctor.watchprotocol.command

import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchSetWatchFaceCommand(dialPos: Int) : WatchCommand(47, run {
    val buf = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN)
    buf.putInt(dialPos)
    buf.array()
})