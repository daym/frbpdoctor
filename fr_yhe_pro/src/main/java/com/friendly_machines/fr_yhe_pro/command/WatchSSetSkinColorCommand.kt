package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

/**
 * TODO: 0: white; 1: yellowish, 2: yellow [the default!], 3: brownish, 4: brown, 5: black
 */
class WatchSSetSkinCommand(skin: Byte) : WatchCommand(WatchOperation.SSetSkin, byteArrayOf(skin)) {
    data class Response(val status: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(status = buf.get())
            }
        }
    }
}
