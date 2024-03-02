package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.commondata.HSleepDataBlock
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory
import java.nio.ByteBuffer

// FIXME: Test
class WatchHGetSleepHistoryCommand : WatchCommand(WatchOperation.HGetSleepHistory, ByteArray(0)) {
    data class Response(val items: List<HSleepDataBlock>) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                // FIXME this is actually very complicated
                val count = buf.remaining() / HSleepDataBlock.SIZE
                return Response(items = WatchResponseFactory.parseDataBlockArray(count, buf) {
                    HSleepDataBlock.parsePro(buf)
                })
            }
        }
    }
}
