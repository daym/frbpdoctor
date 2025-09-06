package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchGGetDeviceInfoCommand : WatchCommand(WatchOperation.GGetDeviceInfo, "GC".toByteArray(Charsets.US_ASCII)) {
    data class Response(
        val deviceId: Short,
        val versionMinor: Byte,
        val versionMajor: Byte,
        val batteryState: Byte,
        val batteryValue: Byte
    ) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                buf.order(ByteOrder.LITTLE_ENDIAN)
                return Response(
                    deviceId = buf.short,
                    versionMinor = buf.get(),
                    versionMajor = buf.get(),
                    batteryState = buf.get(),
                    batteryValue = buf.get()
                )
            }
        }
    }
}
