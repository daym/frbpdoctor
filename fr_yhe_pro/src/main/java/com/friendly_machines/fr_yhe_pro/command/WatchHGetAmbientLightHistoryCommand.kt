package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory
import com.friendly_machines.frbpdoctor.watchprotocol.commondata.HAmbientLightDataBlock
import java.nio.ByteBuffer

class WatchHGetAmbientLightHistoryCommand : WatchCommand(WatchOperation.HGetAmbientLightHistory, ByteArray(0)) {
    data class Response(val items: List<HAmbientLightDataBlock>) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val count = buf.remaining() / HAmbientLightDataBlock.SIZE
                return Response(items = WatchResponseFactory.parseDataBlockArray(count, buf) {
                    HAmbientLightDataBlock.parsePro(buf)
                })
            }
        }
    }
}
