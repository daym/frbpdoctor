package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.commondata.FileVerification
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchCVerifyFileCommand : WatchCommand(WatchOperation.CVerifyFile, byteArrayOf(0)) {
    data class Response(val packageCount: Int, val size: Int, val crc: Int, val fileData: ByteArray) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                buf.order(ByteOrder.LITTLE_ENDIAN)
                val packageCount = if (buf.remaining() >= 4) buf.int else 0
                val size = if (buf.remaining() >= 4) buf.int else 0  
                val crc = if (buf.remaining() >= 2) buf.short.toInt() and 0xFFFF else 0
                
                val remainingData = ByteArray(buf.remaining())
                buf.get(remainingData)
                
                return Response(packageCount = packageCount, size = size, crc = crc, fileData = remainingData)
            }
        }
    }
}
