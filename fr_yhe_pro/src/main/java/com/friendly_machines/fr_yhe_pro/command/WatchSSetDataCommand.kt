package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchSSetDataCommand(data1: String, data2: String, data3: String) : WatchCommand(WatchOperation.SSetData, byteArrayOf(*data1.toByteArray(), *data2.toByteArray(), *data3.toByteArray())) {
    data class Response(val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(status = buf.get())
            }
        }
    }
}
