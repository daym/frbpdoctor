package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * deltaOffset: offset, in Bytes, that we just covered (sending multiple WatchWNextDownloadChunkCommand (which have no answer) before this one)
 * chunkCount: also counts partial packets
 * crc: crc16 of the whole PAYLOAD we sent
 **/
class WatchWNextDownloadChunkMetaCommand(deltaOffset: Int, packetCount: UShort, crc: UShort) : WatchCommand(WatchOperation.WNextDownloadChunkMeta, run {
    val buf = ByteBuffer.allocate(4 + 2 + 2).order(ByteOrder.LITTLE_ENDIAN)
    buf.putInt(deltaOffset)
    buf.putShort(packetCount.toShort())
    buf.putShort(crc.toShort())
    buf.array()
}) {
    data class Response(val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val status = buf.get()
                return Response(status=status)
            }
        }
    }
}
