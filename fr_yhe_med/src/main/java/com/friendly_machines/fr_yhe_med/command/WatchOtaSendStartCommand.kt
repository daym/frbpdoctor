package com.friendly_machines.fr_yhe_med.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_med.WatchOperation
import java.nio.ByteBuffer

class WatchOtaSendStartCommand(type: WatchOtaFirmwareType): WatchCommand(WatchOperation.OtaSendStart, byteArrayOf(type.code))
{
    data class Response(val type: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val type: Byte = buf.get()
                // TODO more stuff, maybe.
                return Response(type = type)
            }
        }
    }
}