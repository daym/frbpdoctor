package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.commondata.FileVerification
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchCVerifyFileCommand : WatchCommand(WatchOperation.CVerifyFile, byteArrayOf(0)) {
    data class Response(val entries: List<FileVerification>) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val entries = mutableListOf<FileVerification>()
                while (buf.remaining() >= 4 + 4 + 2) {
                    entries.add(FileVerification.parsePro(buf))
                }
                return Response(entries = entries)
            }
        }
    }
}
