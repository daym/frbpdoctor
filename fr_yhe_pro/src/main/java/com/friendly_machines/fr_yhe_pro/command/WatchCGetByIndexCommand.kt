package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchCGetByIndexCommand(x: Byte, index: Short): WatchCommand(WatchOperation.CGetByIndex, run {
    val buffer = ByteBuffer.allocate(1 + 2 + 1)
    buffer.put(x)
    buffer.putShort(index)
    buffer.put(1.toByte())
    buffer.array()
}) {

}