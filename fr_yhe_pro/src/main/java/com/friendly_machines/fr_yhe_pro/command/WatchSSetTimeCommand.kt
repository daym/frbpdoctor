package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchSSetTimeCommand(val year: Short, val month: Byte, val day: Byte, val hour: Byte, val minute: Byte, val second: Byte, val weekDay: Byte) : WatchCommand(WatchOperation.SSetTime, run {
    val buf = ByteBuffer.allocate(2 + 1 + 1 + 1 + 1 + 1 + 1).order(ByteOrder.LITTLE_ENDIAN)
    buf.putShort(year)
    buf.put(month)
    buf.put(day)
    buf.put(hour)
    buf.put(minute)
    buf.put(second)
    buf.put(weekDay)
    buf.array()
})