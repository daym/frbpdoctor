package com.friendly_machines.fr_yhe_med.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_med.WatchOperation
import java.nio.ByteBuffer

// See <https://developer.tuya.com/en/docs/iot-device-dev/OTA_BLE> for a possible workflow.
// Purpose: Probably to resume.
// TODO: We could also specify an offset here maybe.
class WatchOtaNegotiateFileOffsetCommand(type: WatchOtaFirmwareType): WatchCommand(WatchOperation.OtaNegotiateFileOffset, byteArrayOf(type.code))
{
    data class Response(val type: Byte, val offset: Int) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val type: Byte = buf.get()
                val offset = buf.int
                return Response(type = type, offset = offset)
            }
        }
    }
}