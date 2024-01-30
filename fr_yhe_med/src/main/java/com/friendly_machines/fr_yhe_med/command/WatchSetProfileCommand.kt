package com.friendly_machines.fr_yhe_med.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchProfileSex
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_med.WatchOperation
import java.nio.ByteBuffer

class WatchSetProfileCommand(height: Byte, weight: Byte, sex: WatchProfileSex, age: Byte) : WatchCommand(WatchOperation.SetProfile, run {
    byteArrayOf(
        height, weight, when (sex) {
            WatchProfileSex.Female -> 0
            WatchProfileSex.Male -> 1
        }, age
    )
}) {
    data class Response(val status: Byte) : // verified
        WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val status = buf.get()
                return Response(status = status)
            }
        }
    }
}