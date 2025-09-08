package com.friendly_machines.fr_yhe_api.commondata

import java.nio.ByteBuffer

data class HeatDataBlockMed(
    val base: Short,
    val walk: Short,
    val sport: Short,
    val dayTimestamp: UInt
) {
    companion object {
        fun parseMed(buf: ByteBuffer): HeatDataBlockMed {
            val base: Short = buf.short
            val walk: Short = buf.short
            val sport: Short = buf.short
            val dayTimestamp: UInt = buf.int.toUInt()
            return HeatDataBlockMed(base, walk, sport, dayTimestamp)
        }
    }
}
