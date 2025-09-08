package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.commondata.HHistoryHeartRateDataBlock
import com.friendly_machines.fr_yhe_pro.WatchOperation
import com.friendly_machines.fr_yhe_pro.indication.WatchBulkResponse
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory
import java.nio.ByteBuffer

// FIXME: Verify
class WatchHGetHeartHistoryCommand : WatchCommand(WatchOperation.HGetHeartHistory, ByteArray(0)) {
    data class Response(val items: List<HHistoryHeartRateDataBlock>) : WatchBulkResponse(WatchOperation.HGetHeartHistory) {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val count = buf.remaining() / HHistoryHeartRateDataBlock.SIZE
                return Response(items = WatchResponseFactory.parseDataBlockArray(count, buf) {
                    HHistoryHeartRateDataBlock.parsePro(buf)
                })
            }
        }
    }
}
