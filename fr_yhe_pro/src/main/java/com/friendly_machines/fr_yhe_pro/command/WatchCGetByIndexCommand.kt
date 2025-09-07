package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import com.friendly_machines.fr_yhe_pro.TimeUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchCGetByIndexCommand(x: Byte, index: Short): WatchCommand(WatchOperation.CGetByIndex, run {
    val buffer = ByteBuffer.allocate(1 + 2 + 1).order(ByteOrder.LITTLE_ENDIAN)
    buffer.put(x)
    buffer.putShort(index)
    buffer.put(1.toByte())
    buffer.array()
}) {
    data class Response(
        val collectType: UByte,
        val collectSN: UShort,  
        val collectSendTime: UInt,
        val collectStartTime: Long,
        val collectTotalLen: UInt,
        val collectBlockNum: UShort,
        val collectDigits: UByte
    ) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                // Case 1 calls DataUnpack.unpackCollectSummaryInfo per line 1186
                buf.order(ByteOrder.LITTLE_ENDIAN)
                val collectType = buf.get().toUByte()
                val collectSN = buf.short.toUShort()
                val collectSendTime = buf.int.toUInt()
                buf.get() // FIXME: 6-byte 6-byte timestamp but we only read 4
                buf.get()
                val collectDigits = buf.get().toUByte()
                val collectTotalLen = buf.int.toUInt()
                val collectBlockNum = buf.short.toUShort()
                
                val collectStartTime = TimeUtils.watchTimeToUnixMillis(collectSendTime.toLong())
                
                return Response(collectType, collectSN, collectSendTime, collectStartTime, collectTotalLen, collectBlockNum, collectDigits)
            }
        }
    }
}