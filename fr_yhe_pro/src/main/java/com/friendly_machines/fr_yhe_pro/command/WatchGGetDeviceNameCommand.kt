package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchGGetDeviceNameCommand : WatchCommand(WatchOperation.GGetDeviceName, "GP".toByteArray(Charsets.US_ASCII)) {
    data class Response(val deviceName: String) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val size = buf.remaining()
                val result = ByteArray(size)
                buf.get(result)
                val deviceName = result.toString(Charsets.UTF_8)
                // FIXME strip zero terminator
                return Response(deviceName = deviceName)
            }
        }
    }
}