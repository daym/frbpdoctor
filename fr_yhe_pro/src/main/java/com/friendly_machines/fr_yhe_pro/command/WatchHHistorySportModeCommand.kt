package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.commondata.HHistorySportModeDataBlock
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import com.friendly_machines.fr_yhe_pro.indication.WatchBulkResponse
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory
import java.nio.ByteBuffer

// FIXME: Test
class WatchHHistorySportModeCommand : WatchCommand(WatchOperation.HHistorySportMode, ByteArray(0)) {
    data class Response(val items: List<HHistorySportModeDataBlock>) : WatchBulkResponse(WatchOperation.HHistorySportMode) {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val count = buf.remaining() / HHistorySportModeDataBlock.SIZE
                return Response(items = WatchResponseFactory.parseDataBlockArray(count, buf) {
                    HHistorySportModeDataBlock.parsePro(buf)
                })
            }
        }
    }
}
