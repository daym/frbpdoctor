package com.friendly_machines.fr_yhe_med.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_med.WatchOperation
import java.nio.ByteBuffer

class WatchGetDeviceConfigCommand : WatchCommand(WatchOperation.GetDeviceConfig, ByteArray(0)) // (big)
{
    data class Response(val body: ByteArray) : WatchResponse() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Response

            if (!body.contentEquals(other.body)) return false

            return true
        }

        override fun hashCode(): Int {
            return body.contentHashCode()
        }

        companion object {
            fun parse(buf: ByteBuffer): Response {
                // big
                val count = buf.remaining()
                val body = ByteArray(count)
                buf.get(body)
                return Response(body)
            }
        }
    }
}