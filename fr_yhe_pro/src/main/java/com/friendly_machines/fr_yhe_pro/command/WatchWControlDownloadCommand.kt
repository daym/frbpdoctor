package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

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
class WatchWControlDownloadCommand(start: Boolean, length: UInt, dialPlateId: Int, blockNumber: Short, version: Short, crc: UShort): WatchCommand(WatchOperation.WControlDownload, run {
    val buf = ByteBuffer.allocate(1 + 4 + 4 + 2 + 2 + 2)
    buf.put(if (start) { 1 } else { 0 })
    buf.putInt(length.toInt())
    if (dialPlateId.toInt() != 0 || crc.toInt() != 0 || version.toInt() != 0 || blockNumber.toInt() != 0) { // FIXME: terrible
        buf.putInt(dialPlateId.toInt())
        buf.putShort(blockNumber.toShort())
        buf.putShort(version.toShort())
        buf.putShort(crc.toShort())
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
