package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory
import com.friendly_machines.fr_yhe_api.commondata.HBloodDataBlock
import java.nio.ByteBuffer

// FIXME: Test
class WatchHGetBloodHistoryCommand : WatchCommand(WatchOperation.HGetBloodHistory, ByteArray(0)) {
    data class Response(val items: List<HBloodDataBlock>) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val count = buf.remaining() / HBloodDataBlock.SIZE
                return Response(items = WatchResponseFactory.parseDataBlockArray(count, buf) {
                    HBloodDataBlock.parsePro(it)
                })
            }
        }
    }
}
