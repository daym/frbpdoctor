package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchCGetByTimestampCommand(x: Byte, timestamp: Long): WatchCommand(WatchOperation.CGetByTimestamp, run {
    val buffer = ByteBuffer.allocate(1 + 4 + 1)
    buffer.put(x)
    buffer.putInt(timestamp.toInt())
    buffer.put(1.toByte())
    buffer.array()
}) {

}