package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchATemperatureCalibrationCommand(temperatureCode: Byte) : WatchCommand(WatchOperation.ATemperatureCalibration, byteArrayOf(temperatureCode)) {
    // FIXME: Response?? None??
    data class Response(val status: Byte = 0) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(status = 0)
            }
        }
    }
}