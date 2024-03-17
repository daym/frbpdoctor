package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.commondata.HComprehensiveMeasurementDataBlock
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory
import java.nio.ByteBuffer

// FIXME: Test
class WatchHGetComprehensiveMeasurementDataCommand : WatchCommand(WatchOperation.HGetComprehensiveMeasurementData, ByteArray(0)) {
    data class Response(val items: List<HComprehensiveMeasurementDataBlock>) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                // FIXME 10 byte here
                val count = buf.remaining() / HComprehensiveMeasurementDataBlock.SIZE
                return Response(items = WatchResponseFactory.parseDataBlockArray(count, buf) {
                    HComprehensiveMeasurementDataBlock.parsePro(buf)
                })
            }
        }
    }
}