package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchGGetDeviceScreenInfoCommand : WatchCommand(WatchOperation.GGetDeviceScreenInfo, ByteArray(0)) {
    // zB a = 176, b = 26, c = 264, d = 24
    data class Response(val a: Short, val b: Short, val c: Short, val d: Short) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(a = buf.short, b = buf.short, c = buf.short, d = buf.short)
            }
        }
    }
}