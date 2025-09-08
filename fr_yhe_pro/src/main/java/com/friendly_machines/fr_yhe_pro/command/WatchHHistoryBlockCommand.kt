package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

/**
 * Health_HistoryBlock acknowledgment command
 * Sends acknowledgment after receiving health history block data
 */
class WatchHHistoryBlockCommand : WatchCommand(WatchOperation.HHistoryBlock, byteArrayOf(0)) { // FIXME: params
    data class Response(val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val status = if (buf.hasRemaining()) buf.get() else 0
                return Response(status = status)
            }
        }
    }
}