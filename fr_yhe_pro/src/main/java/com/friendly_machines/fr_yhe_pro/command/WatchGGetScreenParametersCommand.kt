package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchGGetScreenParametersCommand : WatchCommand(WatchOperation.GGetScreenParameters, ByteArray(0)) {
    data class Response(
        val type: Byte, 
        val width: Short, 
        val height: Short, 
        val corner: Short,
        val clipWidth: Short?,
        val clipHeight: Short?,
        val clipCorner: Short?
    ) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val type = buf.get()
                val width = buf.short
                val height = buf.short  
                val corner = buf.short
                
                // Optional clip dimensions if remaining >= 6 bytes
                val (clipWidth, clipHeight, clipCorner) = if (buf.remaining() >= 6) {
                    Triple(buf.short, buf.short, buf.short)
                } else {
                    Triple(null, null, null)
                }
                
                return Response(type, width, height, corner, clipWidth, clipHeight, clipCorner)
            }
        }
    }
}