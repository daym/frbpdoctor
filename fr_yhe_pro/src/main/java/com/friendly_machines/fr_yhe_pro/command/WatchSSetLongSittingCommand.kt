package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchSSetLongSittingCommand(startHour1: Byte, startMinute1: Byte, endHour1: Byte, endMinute1: Byte, startHour2: Byte, startMinute2: Byte, endHour2: Byte, endMinute2: Byte, repeats: UByte, interval: Byte): WatchCommand(WatchOperation.SSetLongSitting, byteArrayOf(startHour1, startMinute1, endHour1, endMinute1, startHour2, startMinute2, endHour2, endMinute2)) {
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