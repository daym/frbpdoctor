package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchRGetEventReminderCommand : WatchCommand(WatchOperation.REventReminder, byteArrayOf()) {
    data class Response(
        val index: Byte,
        val enabled: Byte,
        val type: Byte,
        val hour: Byte,
        val min: Byte,
        val repeat: Byte,
        val interval: Byte,
        val incidentName: String?
    ) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val index = buf.get()
                val enabled = buf.get()
                val type = buf.get()
                val hour = buf.get()
                val min = buf.get()
                val repeat = buf.get()
                val interval = buf.get()
                
                val incidentName = if ((type.toInt() and 255) == 1 && buf.hasRemaining()) {
                    val nameBytes = ByteArray(buf.remaining())
                    buf.get(nameBytes)
                    String(nameBytes, Charsets.UTF_8)
                } else {
                    null
                }
                
                return Response(index, enabled, type, hour, min, repeat, interval, incidentName)
            }
        }
    }
}
