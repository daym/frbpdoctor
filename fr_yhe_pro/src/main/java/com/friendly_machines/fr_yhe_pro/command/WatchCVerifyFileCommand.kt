package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.commondata.FileVerification
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory
import java.nio.ByteBuffer

class WatchCVerifyFileCommand : WatchCommand(WatchOperation.CVerifyFile, byteArrayOf(0)) {
    data class Response(val items: List<FileVerification>) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val count = buf.remaining() / FileVerification.SIZE
                return Response(items = WatchResponseFactory.parseDataBlockArray(count, buf) {
                    FileVerification.parsePro(buf)
                })
            }
        }
    }
}
