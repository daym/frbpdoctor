package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.commondata.HTemperatureDataBlock
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import com.friendly_machines.fr_yhe_pro.indication.WatchBulkResponse
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory
import java.nio.ByteBuffer

// FIXME: Test
class WatchHGetTemperatureHistoryCommand : WatchCommand(WatchOperation.HGetTemperatureHistory, ByteArray(0)) {
    data class Response(val items: List<HTemperatureDataBlock>) : WatchBulkResponse(WatchOperation.HGetTemperatureHistory) {
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
