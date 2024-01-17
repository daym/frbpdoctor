package com.friendly_machines.frbpdoctor.watchprotocol.command

import java.nio.ByteBuffer
import java.nio.ByteOrder

class DeviceInfoCommand(mtuMinus7: Short) : WatchCommand(0, run {
    val buf = ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN)
    buf.putShort(mtuMinus7)
    buf.array()
})