package com.friendly_machines.fr_yhe_med.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_med.WatchOperation
import java.nio.ByteBuffer

// Example: id=91; voltage=4072
class WatchGetBatteryStateCommand : WatchCommand(WatchOperation.GetBatteryState, ByteArray(0)) {

    data class Response(val id: Byte, val voltage: Short) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val id: Byte = buf.get()
                val voltage: Short = buf.short
                return Response(id = id, voltage = voltage)
            }
        }
    }
}