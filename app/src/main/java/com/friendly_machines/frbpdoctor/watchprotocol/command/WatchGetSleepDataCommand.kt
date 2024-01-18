package com.friendly_machines.frbpdoctor.watchprotocol.command

import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchGetSleepDataCommand(val startTime: Int, val endTime: Int) : WatchCommand(24, run {
    val buf = ByteBuffer.allocate(4 + 4).order(ByteOrder.BIG_ENDIAN)
    buf.putInt(startTime)
    buf.putInt(endTime)
    buf.array()
}) // (big)