package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchGGetMainThemeCommand : WatchCommand(WatchOperation.GGetMainTheme, ByteArray(0)) {
    data class Response(val themeCount: Byte, val currentThemeIndex: Byte) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(themeCount = buf.get(), currentThemeIndex = buf.get())
            }
        }
    }
}