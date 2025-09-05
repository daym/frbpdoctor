package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchCGetByIndexCommand(x: Byte, index: Short): WatchCommand(WatchOperation.CGetByIndex, run {
    val buffer = ByteBuffer.allocate(1 + 2 + 1).order(ByteOrder.LITTLE_ENDIAN)
    buffer.put(x)
    buffer.putShort(index)
    buffer.put(1.toByte())
    buffer.array()
}) {

}