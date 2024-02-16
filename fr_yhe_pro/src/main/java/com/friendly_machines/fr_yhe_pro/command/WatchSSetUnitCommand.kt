package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchSSetUnitCommand(distance: Byte, weight: Byte, temperature: Byte, time24h: Boolean, bloodSugarUnit: Byte, uricAcidUnit: Byte) : WatchCommand(
    WatchOperation.SUnit, byteArrayOf(
        distance, weight, temperature, when (time24h) {
            true -> 1
            false -> 0
        }, bloodSugarUnit, uricAcidUnit
    )
) {
    data class Response(val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(buf.get())
            }
        }
    }
}
