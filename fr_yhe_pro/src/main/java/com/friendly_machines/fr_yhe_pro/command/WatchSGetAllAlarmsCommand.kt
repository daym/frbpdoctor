package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

// SAlarm is used in multiple commands
class WatchSGetAllAlarmsCommand : WatchCommand(WatchOperation.SAlarm, ByteArray(1)) {
    data class Response(val dummy: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val dummy = buf.get() // FIXME
                return Response(dummy = dummy)
            }
        }
    }
}
