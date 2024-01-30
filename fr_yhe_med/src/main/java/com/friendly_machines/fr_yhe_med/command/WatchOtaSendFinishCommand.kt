package com.friendly_machines.fr_yhe_med.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_med.WatchOperation
import java.nio.ByteBuffer

class WatchOtaSendFinishCommand(type: WatchOtaFirmwareType) : WatchCommand(WatchOperation.OtaSendFinish, byteArrayOf(type.code)) {
    data class Response(val type: Byte, val state: Short) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val type: Byte = buf.get()
                val state = buf.short // 1 ok
                return Response(type = type, state = state)
            }
        }
    }
}