package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchGGetElectrodeLocationCommand : WatchCommand(WatchOperation.GGetElectrodeLocation, ByteArray(0)) {
    data class Response(val electrodeLocation: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val electrodeLocation = buf.get() // TODO: convert to enum
                return Response(electrodeLocation = electrodeLocation)
            }
        }
    }
}