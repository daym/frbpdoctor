package com.friendly_machines.fr_yhe_med.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_med.WatchOperation
import java.nio.ByteBuffer

class WatchOtaGetFirmwareVersionCommand(type: WatchOtaFirmwareType) : WatchCommand(WatchOperation.OtaGetFirmwareVersion, byteArrayOf(type.code)) {
    data class Response(val soc: Int, val firmwareVersion: Int) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val soc = buf.int
                val firmwareVersion = buf.int
                return Response(soc = soc, firmwareVersion = firmwareVersion)
            }
        }
    }
}