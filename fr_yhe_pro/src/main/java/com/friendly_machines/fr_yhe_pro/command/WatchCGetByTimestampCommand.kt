package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.commondata.TimeUtils
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchCGetByTimestampCommand(x: Byte, timestamp: Long): WatchCommand(WatchOperation.CGetByTimestamp, run {
    val buffer = ByteBuffer.allocate(1 + 4 + 1).order(ByteOrder.LITTLE_ENDIAN)
    buffer.put(x)
    buffer.putInt(timestamp.toInt())
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
                buf.order(ByteOrder.LITTLE_ENDIAN)
                val collectType = buf.get().toUByte()
                val collectSN = buf.short.toUShort()
                val collectSendTime = buf.int.toUInt()
                buf.get() // FIXME: 6-byte timestamp, we only read 4
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