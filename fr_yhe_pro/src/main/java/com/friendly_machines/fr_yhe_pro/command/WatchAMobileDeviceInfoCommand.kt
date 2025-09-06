package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchAMobileDeviceInfoCommand(deviceInfo: String) : WatchCommand(WatchOperation.AMobileDeviceInfo, deviceInfo.toByteArray(Charsets.UTF_8)) {
    data class Response(val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val bytes = ByteArray(buf.remaining())
                buf.get(bytes)
                val status = if (bytes.isNotEmpty()) bytes.last() else 0
                return Response(status = status)
            }
        }
    }
}
