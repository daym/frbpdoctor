package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchCDeleteByTimestamp(x: Byte, timestamp: Long): WatchCommand(WatchOperation.CDeleteByTimestamp, run {
    val buf = ByteBuffer.allocate(1 + 4)
    buf.put(x)
    buf.putInt(timestamp.toInt())
    buf.array()
}) {
}