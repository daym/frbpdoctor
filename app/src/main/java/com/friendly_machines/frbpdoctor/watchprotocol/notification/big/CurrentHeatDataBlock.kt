package com.friendly_machines.frbpdoctor.watchprotocol.notification.big

import java.nio.ByteBuffer

data class CurrentHeatDataBlock(val base: Short, val walk: Short, val sportInv: Short, val dayTimestamp: UInt) {
    companion object {
        fun parse(buf: ByteBuffer): CurrentHeatDataBlock {
            val base: Short = buf.short
            val walk: Short = buf.short
            val sportInv: Short = buf.short
            val dayTimestamp: UInt = buf.int.toUInt()
            return CurrentHeatDataBlock(base, walk, sportInv, dayTimestamp)
        }
    }
}