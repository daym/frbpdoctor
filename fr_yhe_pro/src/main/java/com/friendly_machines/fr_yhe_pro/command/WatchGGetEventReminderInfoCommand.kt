package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchGGetEventReminderInfoCommand : WatchCommand(WatchOperation.GGetEventReminderInfo, byteArrayOf(1)) {
    data class Response(
        val index: Byte,
        val switch: Byte,
        val type: Byte,
        val hour: Byte,
        val minute: Byte,
        val repeat: Byte,
        val interval: Byte,
        val incidentName: String
    ) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val index = buf.get()
                val switch = buf.get()
                val type = buf.get()
                val hour = buf.get()
                val minute = buf.get()
                val repeat = buf.get()
                val interval = buf.get()
                
                // If type == 1 and there are more bytes, read incident name
                val incidentName = if (type.toInt() == 1 && buf.hasRemaining()) {
                    val nameBytes = ByteArray(buf.remaining())
                    buf.get(nameBytes)
                    String(nameBytes, Charsets.UTF_8)
                } else {
                    ""
                }
                
                return Response(index, switch, type, hour, minute, repeat, interval, incidentName)
            }
        }
    }
}