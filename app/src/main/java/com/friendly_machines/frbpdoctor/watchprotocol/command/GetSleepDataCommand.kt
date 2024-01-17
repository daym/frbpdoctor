package com.friendly_machines.frbpdoctor.watchprotocol.command

class GetSleepDataCommand : WatchCommand(24, run {
//            val buf = ByteBuffer.allocate(4 + 4).order(ByteOrder.BIG_ENDIAN)
//            buf.putInt(startTime)
//            buf.putInt(endTime)
//            return buf.array()
    ByteArray(0)
}) // (big)