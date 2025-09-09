package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

// - Blood Sugar Unit: 0=mmol/L, 1=mg/dL
// - Uric Acid Unit: 0=μmol/L, 1=mg/dL
class WatchSSetUnitCommand(distance: Byte, weight: Byte, temperature: Byte, time24h: Boolean, bloodSugarUnit: Byte, uricAcidUnit: Byte) : WatchCommand(
    WatchOperation.SUnit, byteArrayOf(
        distance, weight, temperature, when (time24h) {
            true -> 1
            false -> 0
        }, bloodSugarUnit, uricAcidUnit
    )
) {
    data class Response(val status: Byte, val data: Byte?) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val status = buf.get()
                val data = if (buf.hasRemaining()) buf.get() else null
                return Response(status = status, data = data)
            }
        }
    }
}
