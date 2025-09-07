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
        val incidentTime: Long,
        val incidentId: Byte,
        val incidentName: String?
    ) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val index = buf.get()
                val enable = buf.get()
                val incidentIndex = buf.get()
                val incidentEnable = buf.get()

                // FIXME: Less terrible
                val rawTime = buf.int.toLong()
                val incidentTime = (rawTime + 946684800L) * 1000L - java.util.TimeZone.getDefault().getOffset(System.currentTimeMillis())
                
                val incidentId = buf.get()
                
                val incidentName = if (buf.hasRemaining()) {
                    val nameBytes = ByteArray(buf.remaining())
                    buf.get(nameBytes)
                    // Find null terminator and strip it
                    val nullIndex = nameBytes.indexOf(0)
                    if (nullIndex >= 0) {
                        String(nameBytes, 0, nullIndex, Charsets.UTF_8)
                    } else {
                        String(nameBytes, Charsets.UTF_8)
                    }
                } else {
                    ""
                }
                
                return Response(index, enable, incidentIndex, incidentEnable, incidentTime, incidentId, incidentName)
            }
        }
    }
}
