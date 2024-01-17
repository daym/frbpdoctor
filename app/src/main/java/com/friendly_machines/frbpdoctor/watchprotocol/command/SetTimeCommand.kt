package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchCommand
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SetTimeCommand(currentTimeInSeconds: Int, timezoneInSeconds: Int) : WatchCommand(
    43, run {
        val buf = ByteBuffer.allocate(4 + 4).order(ByteOrder.BIG_ENDIAN)
        buf.putInt(currentTimeInSeconds)
        buf.putInt(timezoneInSeconds)
        buf.array()
    })