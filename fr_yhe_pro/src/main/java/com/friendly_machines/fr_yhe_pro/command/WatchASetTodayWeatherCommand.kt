package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchASetTodayWeatherCommand(str1: String, str2: String, str3: String, weatherCode: Short) : WatchCommand(WatchOperation.ASetTodayWeather, run {
    val str1Bytes = str1.toByteArray(Charsets.UTF_8)
    val str2Bytes = str2.toByteArray(Charsets.UTF_8)
    val str3Bytes = str3.toByteArray(Charsets.UTF_8)

    val buf = ByteBuffer.allocate(str1Bytes.size + str2Bytes.size + str3Bytes.size + 3 + 3 + 3 + 3 + 2).order(ByteOrder.LITTLE_ENDIAN)
    buf.put(2.toByte())
    buf.putShort(str3Bytes.size.toShort())
    buf.put(str3Bytes)
    buf.put(0.toByte())
    buf.putShort(str1Bytes.size.toShort())
    buf.put(str1Bytes)
    buf.put(1.toByte())
    buf.putShort(str2Bytes.size.toShort())
    buf.put(str2Bytes)
    buf.put(4.toByte())
    buf.put(2.toByte())
    buf.put(0.toByte())
    buf.putShort(weatherCode)
    buf.array()
}) {
    data class Response(val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val status = buf.get() // FIXME
                return Response(status = status)
            }
        }
    }
}