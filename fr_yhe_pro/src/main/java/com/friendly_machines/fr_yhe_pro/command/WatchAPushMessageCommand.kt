package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchAPushMessageCommand(x: Byte, message: String): WatchCommand(WatchOperation.APushMessage, run {
    val messageBytes = message.toByteArray(Charsets.UTF_8)
    var buf = ByteBuffer.allocate(1 + messageBytes.size)
    buf.put(x)
    buf.put(messageBytes)
    buf.array()
})
