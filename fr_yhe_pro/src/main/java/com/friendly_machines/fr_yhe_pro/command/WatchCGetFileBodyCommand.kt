package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchCGetFileBodyCommand(val name: String, val x: Int) : WatchCommand(WatchOperation.CGetFileBody, run {
    // name: max 16 chars in utf-8
    val nameBytes = name.toByteArray(Charsets.UTF_8)
    val buf = ByteBuffer.allocate(16 + 4).order(ByteOrder.LITTLE_ENDIAN)
    buf.put(nameBytes)
    buf.position(16)
    buf.putInt(x)
    buf.array()
})