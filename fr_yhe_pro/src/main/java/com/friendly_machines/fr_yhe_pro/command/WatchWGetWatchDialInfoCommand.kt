package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.commondata.WatchDialDataBlock
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchWGetWatchDialInfoCommand : WatchCommand(WatchOperation.WGetWatchDialInfo, ByteArray(1)) {
    data class Response(val entries: List<WatchDialDataBlock>, val dummy: Byte, val dialCount: UByte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val dummy = buf.get()
                val dialCount = buf.get().toUByte()
                val entries = mutableListOf<WatchDialDataBlock>()
                while (buf.remaining() >= 9) {
                    val entry = WatchDialDataBlock.parsePro(buf)
                    entries.add(entry)
                }
                return Response(dummy = dummy, dialCount = dialCount, entries = entries)
            }
        }
    }
}