package com.friendly_machines.fr_yhe_med.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_med.WatchOperation
import java.nio.ByteBuffer

class WatchGetWatchFaceCommand : WatchCommand(WatchOperation.GetWatchFace, ByteArray(0)) {
    @OptIn(ExperimentalUnsignedTypes::class)
    data class Response(val count: Short, val content: UIntArray) : // verified
        WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val countMinus1: Byte = buf.get()
                val count: Short = (countMinus1 + 1).toShort()
                val arr = UIntArray(count.toInt())
                for (i in 0 until count) {
                    arr[i] = buf.int.toUInt()
                }
                return Response(count, arr)
            }
        }
    }
}