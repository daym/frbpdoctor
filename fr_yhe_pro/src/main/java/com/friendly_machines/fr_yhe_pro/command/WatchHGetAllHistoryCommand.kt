package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.commondata.HAllDataBlock
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import com.friendly_machines.fr_yhe_pro.indication.WatchBulkResponse
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory
import java.nio.ByteBuffer

// FIXME: Test
class WatchHGetAllHistoryCommand : WatchCommand(WatchOperation.HGetAllHistory, ByteArray(0)) {
    data class Response(val items: List<HAllDataBlock>) : WatchBulkResponse(WatchOperation.HGetAllHistory) {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val count = buf.remaining() / HAllDataBlock.SIZE
                return Response(items = WatchResponseFactory.parseDataBlockArray(count, buf) {
                    HAllDataBlock.parsePro(buf)
                })
            }
        }
    }
}
