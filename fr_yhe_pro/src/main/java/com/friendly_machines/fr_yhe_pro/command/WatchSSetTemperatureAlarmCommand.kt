package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchSSetTemperatureAlarmCommand(flag: Byte, temperature: Byte) : WatchCommand(WatchOperation.SSetTemperatureAlarm, byteArrayOf(flag, temperature, 0)) {
    data class Response(val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(buf.get())
            }
        }
    }
}
