package com.friendly_machines.fr_yhe_api.commondata

import java.nio.ByteBuffer

data class CurrentHeatDataBlock(val base: Short, val walk: Short, val sportInv: Short, val dayTimestamp: UInt) {
    companion object {
        fun parseMed(buf: ByteBuffer): CurrentHeatDataBlock {
            val base: Short = buf.short
            val walk: Short = buf.short
            val sportInv: Short = buf.short
            val dayTimestamp: UInt = buf.int.toUInt()
            return CurrentHeatDataBlock(base, walk, sportInv, dayTimestamp)
        }
    }
}