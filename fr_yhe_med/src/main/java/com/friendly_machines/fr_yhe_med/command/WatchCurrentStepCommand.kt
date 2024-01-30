package com.friendly_machines.fr_yhe_med.command

import com.friendly_machines.fr_yhe_api.commondata.CurrentStepDataBlock
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_med.WatchOperation
import java.nio.ByteBuffer

class WatchCurrentStepCommand : WatchCommand(WatchOperation.CurrentStep, ByteArray(0)) // (big)
{
    data class Response(val block: com.friendly_machines.fr_yhe_api.commondata.CurrentStepDataBlock) : WatchResponse() // (big)
    {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                // (big)
                return Response(CurrentStepDataBlock.parseMed(buf))
            }
        }
    }
}