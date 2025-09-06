package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.commondata.DirectoryEntry
import com.friendly_machines.fr_yhe_api.commondata.WatchDialDataBlock
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory
import java.nio.ByteBuffer

class WatchWGetWatchDialInfoCommand : WatchCommand(WatchOperation.WGetWatchDialInfo, ByteArray(1)) {
    data class Response(val items: List<WatchDialDataBlock>, val maxDialCount: Byte, val currentDialCount: UByte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                // VERIFIED: Original SDK unpackDialInfo structure matches WatchDialDataBlock.parsePro exactly
                // byte 0: max dial count, byte 1: current dial count, bytes 2+: dial data
                val maxDialCount = buf.get()
                val currentDialCount = buf.get().toUByte()

                val count = buf.remaining() / WatchDialDataBlock.SIZE
                return Response(maxDialCount = maxDialCount, currentDialCount = currentDialCount, items = WatchResponseFactory.parseDataBlockArray(count, buf) {
                    WatchDialDataBlock.parsePro(buf)
                })
            }
        }
    }
}
