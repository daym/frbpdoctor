package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchRGetEventReminderCommand : WatchCommand(WatchOperation.REventReminder, byteArrayOf()) {
    data class Response(
        val index: Byte,
        val switch: Byte,
        val type: Byte,
        val hour: Byte,
        val min: Byte,
        val repeat: Byte,
        val interval: Byte
    ) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(
                    index = buf.get(),
                    switch = buf.get(),
                    type = buf.get(),
                    hour = buf.get(),
                    min = buf.get(),
                    repeat = buf.get(),
                    interval = buf.get()
                )
            }
        }
    }
}
