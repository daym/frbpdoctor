package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.commondata.HTemperatureAndHumidityDataBlock
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory
import java.nio.ByteBuffer

// FIXME: Test
class WatchHGetTemperatureAndHumidityHistoryCommand : WatchCommand(WatchOperation.HGetTemperatureAndHumidityHistory, ByteArray(0)) {
    data class Response(val items: List<HTemperatureAndHumidityDataBlock>) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val count = buf.remaining() / HTemperatureAndHumidityDataBlock.SIZE
                return Response(items = WatchResponseFactory.parseDataBlockArray(count, buf) {
                    HTemperatureAndHumidityDataBlock.parsePro(buf)
                })
            }
        }
    }
}
