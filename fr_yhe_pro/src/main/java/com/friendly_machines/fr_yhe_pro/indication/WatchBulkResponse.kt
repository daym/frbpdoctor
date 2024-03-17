package com.friendly_machines.fr_yhe_pro.indication

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

open class WatchBulkResponse(operation: WatchOperation) : WatchResponse() {
    companion object {
        data class MainHeader(val totalItemCount: Short, val totalPackageCount: Int, val totalByteCount: Int)
        fun parseMainHeader(buf: ByteBuffer): MainHeader {
            buf.order(ByteOrder.LITTLE_ENDIAN)
            val totalItemCount = buf.short // 688
            val totalPackageCount = buf.int
            val totalByteCount = buf.int
            return MainHeader(totalItemCount, totalPackageCount, totalByteCount)
        }
    }
}