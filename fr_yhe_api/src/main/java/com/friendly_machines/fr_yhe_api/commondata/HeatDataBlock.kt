package com.friendly_machines.fr_yhe_api.commondata

import java.nio.ByteBuffer

data class HeatDataBlock(
    val base: Short,
    val walk: Short,
    val sport: Short,
    val dayTimestamp: UInt
) {
    companion object {
        fun parseMed(buf: ByteBuffer): HeatDataBlock {
            val base: Short = buf.short
            val walk: Short = buf.short
            val sport: Short = buf.short
            val dayTimestamp: UInt = buf.int.toUInt()
            return HeatDataBlock(base, walk, sport, dayTimestamp)
        }
    }
}
