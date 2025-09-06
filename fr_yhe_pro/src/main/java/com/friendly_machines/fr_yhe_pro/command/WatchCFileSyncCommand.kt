package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchCFileSyncCommand(data: ByteArray = byteArrayOf()) : WatchCommand(WatchOperation.CFileSync, data) {
    data class Response(val fileData: ByteArray) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                // FIXME: response at all? No.
                val fileData = ByteArray(buf.remaining())
                buf.get(fileData)
                return Response(fileData = fileData)
            }
        }
    }
}