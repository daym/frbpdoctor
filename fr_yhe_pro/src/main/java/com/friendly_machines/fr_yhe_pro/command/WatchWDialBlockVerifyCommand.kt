package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Block verification command sent after each 4KB block
 */
class WatchWDialBlockVerifyCommand(
    blockSizeBytes: Int, 
    packetCount: Int, 
    blockCrc: UShort
) : WatchCommand(WatchOperation.WNextDownloadChunkMeta, run {
    val buf = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)
    buf.putInt(blockSizeBytes)
    buf.putShort(packetCount.toShort())
    buf.putShort(blockCrc.toShort())
    buf.array()
}) {
    data class Response(val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val status = buf.get() // 0 = success, continue
                return Response(status = status)
            }
        }
    }
}