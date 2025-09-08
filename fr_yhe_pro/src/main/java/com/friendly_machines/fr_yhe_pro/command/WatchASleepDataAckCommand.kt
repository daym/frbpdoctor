package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

// UNUSED
class WatchASleepDataAckCommand(ackCode: Byte, sleepQuality: Byte, deepSleep: Byte, lightSleep: Byte, remSleep: Byte, awakeTime: Byte) : WatchCommand(WatchOperation.ASleepDataAck, run {
    val buf = ByteBuffer.allocate(6).order(ByteOrder.LITTLE_ENDIAN)
    buf.put(ackCode)
    buf.put(sleepQuality)
    buf.put(deepSleep)
    buf.put(lightSleep)
    buf.put(remSleep)
    buf.put(awakeTime)
    buf.array()
}) {
    data class Response(val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val bytes = ByteArray(buf.remaining())
                buf.get(bytes)
                val status = if (bytes.isNotEmpty()) bytes.last() else 0
                return Response(status = status)
            }
        }
    }
}
