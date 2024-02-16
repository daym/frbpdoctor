package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchRGetScheduleCommand : WatchCommand(WatchOperation.RSchedule, byteArrayOf()) {
    data class Response(
        val index: Byte,
        val enable: Byte,
        val incidentIndex: Byte,
        val incidentEnable: Byte,
        val incidentTime: Int,
        val incidentId: Byte
    ) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(
                    index = buf.get(),
                    enable = buf.get(),
                    incidentIndex = buf.get(),
                    incidentEnable = buf.get(),
                    incidentTime = buf.int,
                    incidentId = buf.get()
                )
            }
        }
    }
}
