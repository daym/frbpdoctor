package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.commondata.DirectoryEntry
import com.friendly_machines.fr_yhe_api.commondata.WatchDialDataBlock
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory
import java.nio.ByteBuffer

class WatchWGetWatchDialInfoCommand : WatchCommand(WatchOperation.WGetWatchDialInfo, ByteArray(1)) {
    data class Response(val items: List<WatchDialDataBlock>, val dummy: Byte, val dialCount: UByte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val dummy = buf.get()
                val dialCount = buf.get().toUByte()

                val count = buf.remaining() / WatchDialDataBlock.SIZE
                return Response(dummy = dummy, dialCount = dialCount, items = WatchResponseFactory.parseDataBlockArray(count, buf) {
                    WatchDialDataBlock.parsePro(buf)
                })
            }
        }
    }
}