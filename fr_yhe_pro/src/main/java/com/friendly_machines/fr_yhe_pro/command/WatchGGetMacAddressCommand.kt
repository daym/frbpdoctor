package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchGGetMacAddressCommand : WatchCommand(WatchOperation.GGetMacAddress, ByteArray(0)) {
    // FIXME
    data class Response(val dummy: Unit = Unit) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response()
            }
        }
    }
}