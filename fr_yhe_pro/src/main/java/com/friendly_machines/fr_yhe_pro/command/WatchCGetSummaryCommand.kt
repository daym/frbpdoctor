package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.commondata.FileVerification
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchCGetSummaryCommand: WatchCommand(WatchOperation.CGetSummary, byteArrayOf(0)/*FIXME*/) {
    data class Response(val type: Byte, val sn: Short, val sendTime: Int, val dummy1: Byte, val dummy2: Byte, val digits: UByte, val totalLength: Int, val blockNumber: Short) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(type = buf.get(), sn = buf.short, sendTime = buf.int, dummy1 = buf.get(), dummy2 = buf.get(),  digits = buf.get().toUByte(), totalLength = buf.int, blockNumber = buf.short)
            }
        }
    }

}