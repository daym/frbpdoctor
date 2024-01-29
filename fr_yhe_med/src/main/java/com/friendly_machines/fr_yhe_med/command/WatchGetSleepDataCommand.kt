package com.friendly_machines.fr_yhe_med.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_med.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchGetSleepDataCommand(startTime: Int, endTime: Int) : WatchCommand(WatchOperation.GetSleepData, run {
    val buf = ByteBuffer.allocate(4 + 4).order(ByteOrder.BIG_ENDIAN)
    buf.putInt(startTime)
    buf.putInt(endTime)
    buf.array()
}) // (big)
{
    data class Response(val dummy: Byte): WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                // There seem to be 10 B, all 0.
                return Response(1.toByte())
            }
        }
    }
}