package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchABloodSugarCalibCommand(calibValue1: Byte, calibValue2: Byte, calibValue3: Byte) : WatchCommand(WatchOperation.ABloodSugarCalib, run {
    val buf = ByteBuffer.allocate(3).order(ByteOrder.LITTLE_ENDIAN)
    buf.put(calibValue1)
    buf.put(calibValue2)
    buf.put(calibValue3)
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