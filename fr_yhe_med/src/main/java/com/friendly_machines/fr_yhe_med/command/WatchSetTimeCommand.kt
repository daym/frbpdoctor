package com.friendly_machines.fr_yhe_med.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_med.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchSetTimeCommand(currentTimeInSeconds: Int, timezoneInSeconds: Int) : WatchCommand(
    WatchOperation.SetTime, run {
        val buf = ByteBuffer.allocate(4 + 4).order(ByteOrder.BIG_ENDIAN)
        buf.putInt(currentTimeInSeconds)
        buf.putInt(timezoneInSeconds)
        buf.array()
    }) {
    data class Response(val timestamp: Int, val timezone: Int) : // verified
        WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val timestamp: Int = buf.int
                val timezone: Int = buf.int
                return Response(timestamp = timestamp, timezone = timezone)
            }
        }
    }
}