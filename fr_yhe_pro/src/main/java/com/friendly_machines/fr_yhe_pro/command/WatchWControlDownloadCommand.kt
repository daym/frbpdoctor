package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Example workflow:
- We do WatchWControlDownloadCommand.start(length: UInt, dialPlateId: Int, blockNumber: Short, version: Short, crc: UShort)
    Response: 01 00
- We do WatchWNextDownloadChunkCommand(chunk: ByteArray)
    No response
[...]
- We do WatchWNextDownloadChunkCommand(chunk: ByteArray)  ; total payload: 0x1000 B
    No response
- We do WatchWNextDownloadChunkMetaCommand(deltaOffset: Int = 0x1000, chunkCount: UShort = 0x18, crcPayload: UShort) ; 0x902
    Response: 00
- We do WatchWNextDownloadChunkCommand(chunk: ByteArray)
    No response
[...]
- We do WatchWNextDownloadChunkMetaCommand(deltaOffset: Int = 0x1000, chunkCount: UShort = 0x18, crcPayload: UShort) ; 0x902     ; apparently, we always target 0x1000
    Response: 00
- We do WatchWControlDownloadCommand.stop(length) and nothing else
    Response: 00 00

Afterwards, regular operation:

- We do WatchWGetWatchDialInfoCommand
    Response: Watch dial info
- We do WSetCurrentWatchDial
 */
class WatchWControlDownloadCommand(start: Boolean, length: UInt, dialPlateId: Int, blockNumber: Short, version: Short, crc: UShort, private val responseCallback: ((Response) -> Unit)? = null): WatchCommand(WatchOperation.WControlDownload, run {
    val buf = if (start) {
        // Start command: always 15 bytes
        ByteBuffer.allocate(1 + 4 + 4 + 2 + 2 + 2).order(ByteOrder.LITTLE_ENDIAN).apply {
            put(1)
            putInt(length.toInt())
            putInt(dialPlateId.toInt())
            putShort(blockNumber.toShort())
            putShort(version.toShort())
            putShort(crc.toShort())
        }
    } else {
        // Stop command: only 5 bytes (flag + length)
        ByteBuffer.allocate(1 + 4).order(ByteOrder.LITTLE_ENDIAN).apply {
            put(0)
            putInt(length.toInt())
        }
    }
    buf.array()
}) {
    data class Response(val ok: Byte, val errorCode: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val ok = buf.get() // FIXME; maybe 0 is ok
                val errorCode = buf.get() // FIXME
                return Response(ok=ok, errorCode=errorCode)
            }
        }
    }
    companion object {
        fun start(length: UInt, dialPlateId: Int, blockNumber: Short, version: Short, crc: UShort): WatchWControlDownloadCommand = WatchWControlDownloadCommand(
                start=true, length=length, dialPlateId=dialPlateId, blockNumber=blockNumber, version=version, crc=crc
        )
        fun stop(length: UInt): WatchWControlDownloadCommand = WatchWControlDownloadCommand(false, length, 0, 0, 0, 0U) // Note: the original only sends flag and length most of the time
    }
}
