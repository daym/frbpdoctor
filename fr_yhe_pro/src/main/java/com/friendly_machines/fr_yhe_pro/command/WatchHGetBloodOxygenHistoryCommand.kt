package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.commondata.HBloodOxygenDataBlock
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import com.friendly_machines.fr_yhe_pro.indication.WatchBulkResponse
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory
import java.nio.ByteBuffer

// FIXME: Test
class WatchHGetBloodOxygenHistoryCommand : WatchCommand(WatchOperation.HGetBloodOxygenHistory, ByteArray(0)) {
    data class Response(val items: List<HBloodOxygenDataBlock>) : WatchBulkResponse(WatchOperation.HGetBloodOxygenHistory) {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val count = buf.remaining() / HBloodOxygenDataBlock.SIZE
                return Response(items = WatchResponseFactory.parseDataBlockArray(count, buf) {
                    HBloodOxygenDataBlock.parsePro(buf)
                })
            }
        }
    }
}