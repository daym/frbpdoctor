package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchWDeleteWatchDialCommand(val id: Int) : WatchCommand(WatchOperation.WDeleteWatchDial, run {
    val buf = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
    buf.putInt(id)
    buf.array()
})