package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.commondata.HFallDataBlock
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import com.friendly_machines.fr_yhe_pro.indication.WatchBulkResponse
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory
import java.nio.ByteBuffer

// FIXME: Test
class WatchHGetFallHistoryCommand : WatchCommand(WatchOperation.HGetFallHistory, ByteArray(0)) {
    data class Response(val items: List<HFallDataBlock>) : WatchBulkResponse(WatchOperation.HGetFallHistory) {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val count = buf.remaining() / HFallDataBlock.SIZE
                return Response(items = WatchResponseFactory.parseDataBlockArray(count, buf) {
                    HFallDataBlock.parsePro(buf)
                })
            }
        }
    }
}
