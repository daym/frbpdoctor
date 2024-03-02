package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchGGetRealTemperatureCommand : WatchCommand(WatchOperation.GGetRealTemperature, ByteArray(0)) {
    data class Response(val temperatureInteger: Byte, val temperatureFloat: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val valueInteger = buf.get()
                val valueFloat = buf.get()
                return Response(valueInteger, valueFloat)
            }
        }
    }
}