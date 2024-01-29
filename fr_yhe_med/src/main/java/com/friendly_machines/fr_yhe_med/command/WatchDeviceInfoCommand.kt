package com.friendly_machines.fr_yhe_med.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_med.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchDeviceInfoCommand(maxAttPayloadSize: Short) : WatchCommand(WatchOperation.DeviceInfo, run {
    val buf = ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN)
    buf.putShort(maxAttPayloadSize)
    buf.array()
}) {
    data class Response(val romVersion: Int, val soc: Int, val protocolVersion: Short) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                // I think 4.2 is the protocol version
                val romVersion = buf.int
                val soc = buf.int
                val protocolVersion = buf.short // this might be the protocol version (4.2)
                // It has a lot of other crap, too. The next byte is maybe a "bound" flag
                return Response(romVersion = romVersion, soc = soc, protocolVersion = protocolVersion)
            }
        }
    }
}