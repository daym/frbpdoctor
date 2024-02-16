package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.commondata.DirectoryEntry
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchCGetFileListCommand(x: Short, y: Short) : WatchCommand(WatchOperation.CGetFileList, run {
    val buf = ByteBuffer.allocate(2 + 2)
    buf.putShort(x)
    buf.putShort(y)
    buf.array()
}) {
    data class Response(val entries: List<DirectoryEntry>) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                // in chunks of 24; 16 of that into name. Then 4 of that into file size. Then 4 of that into file checksum
                val entries = mutableListOf<DirectoryEntry>()
                while (buf.remaining() >= 24) {
                    val entry = DirectoryEntry.parsePro(buf)
                    entries.add(entry)
                }
                return Response(entries = entries)
            }
        }
    }
}