package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.commondata.HHeartDataBlock
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import com.friendly_machines.fr_yhe_pro.indication.WatchBulkResponse
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory
import java.nio.ByteBuffer

// FIXME: Test
class WatchHGetHeartHistoryCommand : WatchCommand(WatchOperation.HGetHeartHistory, ByteArray(0)) {
    data class Response(val items: List<HHeartDataBlock>) : WatchBulkResponse(WatchOperation.HGetHeartHistory) {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val count = buf.remaining() / HHeartDataBlock.SIZE
                return Response(items = WatchResponseFactory.parseDataBlockArray(count, buf) {
                    HHeartDataBlock.parsePro(buf)
                })
            }
        }
    }
}
