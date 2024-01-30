package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory
import com.friendly_machines.fr_yhe_api.commondata.HHealthMonitoringDataBlock
import java.nio.ByteBuffer

// FIXME: Test
class WatchHGetHealthMonitoringHistoryCommand : WatchCommand(WatchOperation.HGetHealthMonitoringHistory, ByteArray(0)) {
    data class Response(val items: List<HHealthMonitoringDataBlock>) : WatchResponse() {

        companion object {
            fun parse(buf: ByteBuffer): Response {
                val count = buf.remaining() / HHealthMonitoringDataBlock.SIZE
                return Response(items = WatchResponseFactory.parseDataBlockArray(count, buf) {
                    HHealthMonitoringDataBlock.parsePro(buf)
                })
            }
        }
    }
}