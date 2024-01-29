package com.friendly_machines.fr_yhe_med.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_med.WatchOperation
import java.nio.ByteBuffer

class WatchGetSportDataCommand : WatchCommand(WatchOperation.GetSportData, ByteArray(0)) // (big)
{
    data class Response(val count: Int) : WatchResponse() { // verified
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Response

            if (count != other.count) return false

            return true
        }

        override fun hashCode(): Int {
            return count
        }

        companion object {
            fun parse(buf: ByteBuffer): Response {
                val count = buf.int
//                val b = ByteArray(buf.remaining()) // [0]
//                buf.get(b)
                return Response(count = count)
            }
        }
    }
}