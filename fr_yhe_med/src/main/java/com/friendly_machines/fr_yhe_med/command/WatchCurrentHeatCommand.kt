package com.friendly_machines.fr_yhe_med.command

import com.friendly_machines.fr_yhe_med.WatchOperation
import com.friendly_machines.fr_yhe_api.commondata.CurrentHeatDataBlock
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import java.nio.ByteBuffer

class WatchCurrentHeatCommand : WatchCommand(WatchOperation.CurrentHeat, ByteArray(0)) // (big)
{
    data class Response(val data: com.friendly_machines.fr_yhe_api.commondata.CurrentHeatDataBlock) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                // current heat (big)
                val item = CurrentHeatDataBlock.parseMed(buf)
                return Response(item)
            }
        }
    }
}