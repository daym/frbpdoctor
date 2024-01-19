package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchGetSleepDataCommand(startTime: Int, endTime: Int) : WatchCommand(WatchOperation.GetSleepData, run {
    val buf = ByteBuffer.allocate(4 + 4).order(ByteOrder.BIG_ENDIAN)
    buf.putInt(startTime)
    buf.putInt(endTime)
    buf.array()
}) // (big)