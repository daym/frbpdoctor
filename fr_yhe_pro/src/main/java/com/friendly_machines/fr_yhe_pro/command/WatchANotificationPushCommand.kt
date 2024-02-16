package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class WatchANotificationPushCommand(val type: Byte, val title: String, val str2: String) : WatchCommand(WatchOperation.ANotificationPush, run {
    val titleBytes = title.toByteArray(StandardCharsets.UTF_8)
    val str2Bytes = str2.toByteArray(StandardCharsets.UTF_8)
    val output = ByteArrayOutputStream()
    output.write(byteArrayOf(type))
    output.write(titleBytes)
    output.write(byteArrayOf(0))
    output.write(str2Bytes)
    output.write(byteArrayOf(0))
    output.toByteArray()
}) {
    data class Response(val dummy: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val dummy = buf.get() // FIXME
                return Response(dummy = dummy)
            }
        }
    }
}