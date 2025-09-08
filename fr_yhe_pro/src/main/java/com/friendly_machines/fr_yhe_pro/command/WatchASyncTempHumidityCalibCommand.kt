package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchASyncTempHumidityCalibCommand(tempOffset: Byte, humidityOffset: Byte, tempScale: Byte, humidityScale: Byte) : WatchCommand(WatchOperation.ASyncTempHumidityCalib, run {
    val buf = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
    buf.put(tempOffset)
    buf.put(humidityOffset)
    buf.put(tempScale)
    buf.put(humidityScale)
    buf.array()
}) {
    data class Response(val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val status = if (buf.hasRemaining()) {
                    buf.position(buf.limit() - 1)
                    buf.get()
                } else 0
                return Response(status = status)
            }
        }
    }
}