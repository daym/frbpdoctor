package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.commondata.SportState
import com.friendly_machines.fr_yhe_api.commondata.SportType
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchASetSportModeCommand(sportState: SportState, sportType: SportType) : WatchCommand(WatchOperation.ASetSportMode, byteArrayOf(sportState.value, sportType.value)) {
    
    data class Response(val status: Byte = 0) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                // ignore
                return Response(status = 0)
            }
        }
    }
}