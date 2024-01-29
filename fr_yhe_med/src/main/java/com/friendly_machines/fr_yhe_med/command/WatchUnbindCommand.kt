package com.friendly_machines.fr_yhe_med.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_med.WatchOperation
import java.nio.ByteBuffer

class WatchUnbindCommand : WatchCommand(WatchOperation.Unbind, ByteArray(0)) {
    data class Response(
        val status: Byte
    ) : WatchResponse() // kinda verified
    {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val status: Byte = buf.get()
                return Response(status = status)
            }
        }
    }
}