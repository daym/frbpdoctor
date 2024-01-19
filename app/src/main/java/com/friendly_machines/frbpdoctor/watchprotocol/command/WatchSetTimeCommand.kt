package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchSetTimeCommand(currentTimeInSeconds: Int, timezoneInSeconds: Int) : WatchCommand(
    WatchOperation.SetTime, run {
        val buf = ByteBuffer.allocate(4 + 4).order(ByteOrder.BIG_ENDIAN)
        buf.putInt(currentTimeInSeconds)
        buf.putInt(timezoneInSeconds)
        buf.array()
    })