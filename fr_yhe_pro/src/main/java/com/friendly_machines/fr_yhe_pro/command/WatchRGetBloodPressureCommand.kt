package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchRGetBloodPressureCommand : WatchCommand(WatchOperation.RBloodPressure, byteArrayOf()) {
    data class Response(val systolicPressure: Byte, val diastolicPressure: Byte, val heartValue: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(
                    systolicPressure = buf.get(),
                    diastolicPressure = buf.get(),
                    heartValue = buf.get()
                )
            }
        }
    }
}
