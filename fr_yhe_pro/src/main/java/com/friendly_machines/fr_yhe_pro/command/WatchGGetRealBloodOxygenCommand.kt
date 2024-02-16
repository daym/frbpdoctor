package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchGGetRealBloodOxygenCommand : WatchCommand(WatchOperation.GGetRealBloodOxygen, "IS".toByteArray(Charsets.US_ASCII)) {
    data class Response(val dummy: Byte, val dummy2: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val dummy = buf.get() // FIXME
                val dummy2 = buf.get() // FIXME
                return Response(dummy = dummy, dummy2 = dummy2)
            }
        }
    }
}