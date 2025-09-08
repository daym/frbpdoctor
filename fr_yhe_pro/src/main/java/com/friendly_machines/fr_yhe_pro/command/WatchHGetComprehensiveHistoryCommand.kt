package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.commondata.HHistoryComprehensiveMeasurementDataBlock
import com.friendly_machines.fr_yhe_pro.WatchOperation
import com.friendly_machines.fr_yhe_pro.indication.WatchBulkResponse
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory
import java.nio.ByteBuffer

// FIXME: Test
class WatchHGetComprehensiveHistoryCommand : WatchCommand(WatchOperation.HGetComprehensiveMeasurementData, ByteArray(0)) {
    data class Response(val items: List<HHistoryComprehensiveMeasurementDataBlock>) : WatchBulkResponse(WatchOperation.HGetComprehensiveMeasurementData) {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                // FIXME 10 byte here
                val count = buf.remaining() / HHistoryComprehensiveMeasurementDataBlock.SIZE
                return Response(items = WatchResponseFactory.parseDataBlockArray(count, buf) {
                    HHistoryComprehensiveMeasurementDataBlock.parsePro(buf)
                })
            }
        }
    }
}