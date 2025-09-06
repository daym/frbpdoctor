package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.commondata.DirectoryEntry
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchCGetFileListCommand(x: Short, y: Short) : WatchCommand(WatchOperation.CGetFileList, run {
    val buf = ByteBuffer.allocate(2 + 2).order(ByteOrder.LITTLE_ENDIAN)
    buf.putShort(x)
    buf.putShort(y)
    buf.array()
}) {
    data class Response(val items: List<DirectoryEntry>) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                buf.order(ByteOrder.LITTLE_ENDIAN)
                val count = buf.remaining() / DirectoryEntry.SIZE
                return Response(items = WatchResponseFactory.parseDataBlockArray(count, buf) {
                    DirectoryEntry.parsePro(buf)
                })
            }
        }
    }
}