package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

/**
 * Sets all 6 unit preferences on the watch atomically.
 * 
 * @param distance Distance unit: 0=kilometers, 1=miles
 * @param weight Weight unit: 0=kilograms, 1=pounds  
 * @param temperature Temperature unit: 0=Celsius, 1=Fahrenheit
 * @param timeFormat Time format: 0=24-hour, 1=12-hour
 * @param bloodSugarUnit Blood sugar unit: 0=mmol/L, 1=mg/dL
 * @param uricAcidUnit Uric acid unit: 0=μmol/L, 1=mg/dL
 */
class WatchSSetUnitCommand(distance: Byte, weight: Byte, temperature: Byte, timeFormat: Byte, bloodSugarUnit: Byte, uricAcidUnit: Byte) : WatchCommand(
    WatchOperation.SUnit, byteArrayOf(
        distance, weight, temperature, timeFormat, bloodSugarUnit, uricAcidUnit
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
