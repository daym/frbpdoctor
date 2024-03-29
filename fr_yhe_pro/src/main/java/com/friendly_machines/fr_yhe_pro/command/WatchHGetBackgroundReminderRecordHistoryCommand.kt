package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.commondata.HBackgroundReminderRecordDataBlock
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory
import java.nio.ByteBuffer

// FIXME: Test
class WatchHGetBackgroundReminderRecordHistoryCommand : WatchCommand(WatchOperation.HGetBackgroundReminderRecordHistory, ByteArray(0)) {
    data class Response(val items: List<HBackgroundReminderRecordDataBlock>) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val count = buf.remaining() / HBackgroundReminderRecordDataBlock.SIZE
                return Response(items = WatchResponseFactory.parseDataBlockArray(count, buf) {
                    HBackgroundReminderRecordDataBlock.parsePro(buf)
                })
            }
        }
    }
}