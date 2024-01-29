package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

// mode: 0 or 1
class WatchSSetDndModeCommand(val mode: Byte, val startTimeHour: Byte, val startTimeMin: Byte, val endTimeHour: Byte, val endTimeMin: Byte) : WatchCommand(WatchOperation.SSetDnd, run {
    val buf = ByteBuffer.allocate(5)
    buf.put(mode)
    buf.put(startTimeHour)
    buf.put(startTimeMin)
    buf.put(endTimeHour)
    buf.put(endTimeMin)
    buf.array()
})