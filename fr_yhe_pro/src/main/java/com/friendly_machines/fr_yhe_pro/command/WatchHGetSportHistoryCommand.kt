package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.commondata.HSportDataBlock
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import com.friendly_machines.fr_yhe_pro.indication.WatchBulkResponse
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory
import java.nio.ByteBuffer

// FIXME: Test
class WatchHGetSportHistoryCommand : WatchCommand(WatchOperation.HGetSportHistory, ByteArray(0)) {
    data class Response(val items: List<HSportDataBlock>) : WatchBulkResponse(WatchOperation.HGetSportHistory) {

        companion object {
            fun parse(buf: ByteBuffer): Response {
                val count = buf.remaining() / HSportDataBlock.SIZE
                return Response(items = WatchResponseFactory.parseDataBlockArray(count, buf) {
                    HSportDataBlock.parsePro(buf)
                })
            }
        }
    }
}
