package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchSetStepGoalCommand(steps: Int) : WatchCommand(WatchOperation.SetStepGoal, run {
    val buf = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN)
    buf.putInt(steps)
    buf.array()
})