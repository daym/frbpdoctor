package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchCDeleteByIndex(x: Byte, index: Short): WatchCommand(WatchOperation.CDeleteByIndex, run {
    val buf = ByteBuffer.allocate(1 + 2 + 2).order(ByteOrder.LITTLE_ENDIAN)
    buf.put(x)
    buf.putShort(index)
    buf.array()
}) {
}