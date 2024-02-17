package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.commondata.SkinColor
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchSSetSkinColorCommand(skinColor: SkinColor) : WatchCommand(WatchOperation.SSetSkin, byteArrayOf(when (skinColor) {
    SkinColor.White -> 0
    SkinColor.Yellowish -> 1
    SkinColor.Yellow -> 2
    SkinColor.Brownish -> 3
    SkinColor.Brown -> 4
    SkinColor.Black -> 5
})) {
    data class Response(val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(status = buf.get())
            }
        }
    }
}
