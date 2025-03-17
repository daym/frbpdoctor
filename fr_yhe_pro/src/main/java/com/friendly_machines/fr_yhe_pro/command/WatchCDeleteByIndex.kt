package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchCDeleteByIndex(x: Byte, index: Short): WatchCommand(WatchOperation.CDeleteByIndex, run {
    val buf = ByteBuffer.allocate(1 + 2 + 2)
    buf.put(x)
    buf.putShort(index)
    buf.array()
}) {
}