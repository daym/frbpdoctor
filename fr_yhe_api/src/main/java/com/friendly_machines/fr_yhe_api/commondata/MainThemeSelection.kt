package com.friendly_machines.fr_yhe_api.commondata

import java.nio.ByteBuffer

data class MainThemeSelection(val index: Byte, val count: Byte) {
    companion object {
        fun parsePro(buf: ByteBuffer): MainThemeSelection {
            return MainThemeSelection(count = buf.get(), index = buf.get())
        }
    }
}