package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchSetWatchFaceCommand(dialPos: Int) : WatchCommand(WatchOperation.SetWatchFace, run {
    val buf = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN)
    buf.putInt(dialPos)
    buf.array()
})