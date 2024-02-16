package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_api.commondata.WatchWearingArm
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchSSetWatchWearingArmCommand(val arm: WatchWearingArm) : WatchCommand(WatchOperation.SSetWatchWearingArm, byteArrayOf(when(arm) {
    WatchWearingArm.Left -> 0
    WatchWearingArm.Right -> 1
})) {
    data class Response(val dummy: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val dummy = buf.get() // FIXME
                return Response(dummy = dummy)
            }
        }
    }
}
