package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchDeviceInfoCommand(maxAttPayloadSize: Short) : WatchCommand(WatchOperation.DeviceInfo, run {
    val buf = ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN)
    buf.putShort(maxAttPayloadSize)
    buf.array()
})