package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory
import com.friendly_machines.fr_yhe_api.commondata.HTemperatureDataBlock
import java.nio.ByteBuffer

// FIXME: Test
class WatchHGetTemperatureHistoryCommand : WatchCommand(WatchOperation.HGetTemperatureHistory, ByteArray(0)) {
    data class Response(val items: List<HTemperatureDataBlock>) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val count = buf.remaining() / HTemperatureDataBlock.SIZE
                return Response(items = WatchResponseFactory.parseDataBlockArray(count, buf) {
                    HTemperatureDataBlock.parsePro(buf)
                })
            }
        }
    }
}
