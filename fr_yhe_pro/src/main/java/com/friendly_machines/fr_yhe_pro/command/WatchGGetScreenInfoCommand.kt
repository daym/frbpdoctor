package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchGGetScreenInfoCommand : WatchCommand(WatchOperation.GGetScreenInfo, ByteArray(0)) {
    data class Response(val displayLevel: Byte, val offTime: Byte, val language: Byte, val workMode: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(displayLevel = buf.get(), offTime = buf.get(), language = buf.get(), workMode = buf.get())
            }
        }
    }
}