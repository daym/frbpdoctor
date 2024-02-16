package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchSSetUserInfoCommand(val height: Int, val weight: Int/*kg*/, sex: Byte, age: Byte): WatchCommand(WatchOperation.SUserInfo, byteArrayOf(height.toByte(), weight.toByte(), sex, age)) {
    data class Response(val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(status = buf.get())
            }
        }
    }
}