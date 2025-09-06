package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchGGetRealBloodOxygenCommand : WatchCommand(WatchOperation.GGetRealBloodOxygen, "IS".toByteArray(Charsets.US_ASCII)) {
    data class Response(val isTest: Byte, val bloodOxygenValue: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(
                    isTest = buf.get(),
                    bloodOxygenValue = buf.get()
                )
            }
        }
    }
}