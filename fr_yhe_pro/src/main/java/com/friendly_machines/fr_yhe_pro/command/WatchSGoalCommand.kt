package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchSGoalCommand(
    goalType: Byte,      // 0 (step goal), 3 (sleep quality)
    stepGoal: Int = 0,
    sleepQuality: Byte = 0,
    reserved: Byte = 0
) : WatchCommand(WatchOperation.SGoal, run {
    val buf = ByteBuffer.allocate(7).order(ByteOrder.LITTLE_ENDIAN)
    buf.put(goalType)
    buf.putInt(stepGoal)
    buf.put(sleepQuality)
    buf.put(reserved)
    buf.array()
}) {
    data class Response(val status: Byte, val data: Byte?) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val status = buf.get()
                val data = if (buf.hasRemaining()) buf.get() else null
                return Response(status = status, data = data)
            }
        }
    }
}
