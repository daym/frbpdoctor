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
                // Find null terminator and strip it
                val nullIndex = result.indexOf(0)
                val deviceName = if (nullIndex >= 0) {
                    result.sliceArray(0 until nullIndex).toString(Charsets.UTF_8)
                } else {
                    result.toString(Charsets.UTF_8)
                }
                return Response(deviceName = deviceName)
            }
        }
    }
}